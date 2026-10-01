"""Build and publish a paired Magneticraft release; no third-party Python dependencies."""
import hashlib
import json
import os
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

TARGETS = {"1.21.1": "21", "26.3": "25"}
VERSION = re.compile(r"[0-9]+\.[0-9]+\.[0-9]+(?:[ab])?(?:\.R[0-9]+)?")

def gh(*args, check=True):
    return subprocess.run(["gh", *args], check=check, capture_output=True, text=True)

def api(path):
    return json.loads(gh("api", path).stdout)

def properties(text):
    return dict(line.split("=", 1) for line in text.splitlines()
                if "=" in line and not line.lstrip().startswith("#"))

def checked_version(value):
    if not VERSION.fullmatch(value):
        raise ValueError(f"Unsupported release version: {value!r}")
    return value

def prepare():
    repo = os.environ["GH_REPO"]
    entries, versions = [], []
    for target, java in TARGETS.items():
        sha = api(f"repos/{repo}/git/ref/heads/{target}")["object"]["sha"]
        raw = gh("api", f"repos/{repo}/contents/gradle.properties?ref={sha}",
                 "-H", "Accept: application/vnd.github.raw+json").stdout
        props = properties(raw)
        if props["minecraft_version"] != target:
            raise ValueError(f"Wrong Minecraft target on branch {target}")
        versions.append(checked_version(props["mod_version"]))
        entries.append({"target": target, "java": java, "sha": sha})
    with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
        if len(set(versions)) != 1:
            print(f"Waiting for matching versions: {dict(zip(TARGETS, versions))}")
            output.write("ready=false\n")
            return
        output.write(f"ready=true\nversion={versions[0]}\n")
        output.write("matrix=" + json.dumps({"include": entries}) + "\n")

def package():
    target = os.environ["MC_TARGET"]
    version = checked_version(os.environ["RELEASE_VERSION"])
    sha = os.environ["SOURCE_SHA"]
    props = properties(pathlib.Path("gradle.properties").read_text())
    if props["mod_version"] != version or props["minecraft_version"] != target:
        raise ValueError("Source properties do not match release plan")
    name = f"Foundations-Magneticraft-{target}-{version}.jar"
    source = pathlib.Path("build/libs") / name
    with zipfile.ZipFile(source) as jar:
        names = set(jar.namelist())
        required = {"META-INF/neoforge.mods.toml", "LICENSE",
                    "com/foundations/magneticraft/FoundationsMagneticraft.class"}
        if not required.issubset(names):
            raise ValueError(f"JAR is missing required files: {required - names}")
        metadata = jar.read("META-INF/neoforge.mods.toml").decode()
        if version not in metadata or "${" in metadata:
            raise ValueError("Unexpanded or incorrect mod metadata")
    destination = pathlib.Path("release")
    destination.mkdir(exist_ok=True)
    shutil.copy2(source, destination / name)
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    (destination / "SHA256SUMS.txt").write_text(f"{digest}  {name}\n")
    manifest = {"target": target, "version": version, "source_sha": sha,
                "neoforge": props["neo_version"], "java": TARGETS[target],
                "artifact": name, "sha256": digest}
    (destination / "release-manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    notes = pathlib.Path("PORT_STATUS.md").read_text().replace(
        "No release should be published yet.", "Early alpha: incomplete port; review limitations below.")
    (destination / "release-notes.md").write_text(
        f"# Foundations Magneticraft {version} — Minecraft {target}\n\n"
        f"Source: {sha}\nNeoForge: {props['neo_version']}\nJava: {TARGETS[target]}\n\n"
        "Validation: compilation, Gradle checks, and JAR packaging. "
        "Runtime testing is recorded separately below.\n\n" + notes)

def verified_bundle(directory, target, version):
    manifest = json.loads((directory / "release-manifest.json").read_text())
    if manifest["target"] != target or manifest["version"] != version:
        raise ValueError("Wrong release artifact target or version")
    expected = f"Foundations-Magneticraft-{target}-{version}.jar"
    if manifest["artifact"] != expected:
        raise ValueError("Unexpected artifact name")
    if hashlib.sha256((directory / expected).read_bytes()).hexdigest() != manifest["sha256"]:
        raise ValueError("Release checksum mismatch")
    if not re.fullmatch(r"[a-f0-9]{40}", manifest["source_sha"]):
        raise ValueError("Invalid source SHA")
    return manifest

def publish():
    repo = os.environ["GH_REPO"]
    version = checked_version(os.environ["RELEASE_VERSION"])
    # Validate BOTH bundles before any external write.
    bundles = {t: pathlib.Path("artifacts") / f"release-{t}" for t in TARGETS}
    manifests = {t: verified_bundle(d, t, version) for t, d in bundles.items()}
    # Reject existing tags pointing to different source commits, including annotated tags.
    for target in TARGETS:
        tag = f"mc{target}-v{version}"
        result = gh("api", f"repos/{repo}/git/ref/tags/{tag}", check=False)
        if result.returncode == 0:
            obj = json.loads(result.stdout)["object"]
            while obj["type"] == "tag":
                obj = api(f"repos/{repo}/git/tags/{obj['sha']}")["object"]
            if obj["type"] != "commit" or obj["sha"] != manifests[target]["source_sha"]:
                raise ValueError(f"{tag} refers to different source; use a revision")
        elif "404" not in result.stderr:
            raise RuntimeError(f"Cannot verify tag: {result.stderr}")
    pending = []
    for target, directory in bundles.items():
        tag = f"mc{target}-v{version}"
        release_result = gh("release", "view", tag, "--repo", repo,
                            "--json", "isDraft", check=False)
        if release_result.returncode == 0:
            draft = json.loads(release_result.stdout)["isDraft"]
            with tempfile.TemporaryDirectory() as temporary:
                downloaded = gh("release", "download", tag, "--repo", repo,
                                "--pattern", "release-manifest.json", "--dir", temporary,
                                check=False)
                if downloaded.returncode == 0:
                    previous = json.loads((pathlib.Path(temporary) /
                                           "release-manifest.json").read_text())
                    if previous != manifests[target]:
                        raise ValueError(f"{tag} already has different source/artifacts; use a revision")
                elif not draft:
                    raise ValueError(f"Published {tag} has no verifiable manifest")
            if not draft:
                print(f"Already published and verified: {tag}")
                continue
        else:
            # A failed lookup must be a real 404, not an authentication/network error.
            lookup = gh("api", f"repos/{repo}/releases/tags/{tag}", check=False)
            if lookup.returncode == 0 or "404" not in lookup.stderr:
                raise RuntimeError(f"Cannot safely determine release state: {lookup.stderr}")
            args = ["release", "create", tag, "--repo", repo, "--draft",
                    "--target", manifests[target]["source_sha"],
                    "--title", f"Foundations Magneticraft {version} — Minecraft {target}",
                    "--notes-file", str(directory / "release-notes.md")]
            if re.search(r"[ab]", version):
                args.append("--prerelease")
            gh(*args)
        # Draft assets can be completed on retry; published assets are never overwritten.
        gh("release", "upload", tag, "--repo", repo, "--clobber",
           str(directory / manifests[target]["artifact"]),
           str(directory / "SHA256SUMS.txt"), str(directory / "release-manifest.json"))
        # Confirm uploaded asset bytes before publishing either draft.
        with tempfile.TemporaryDirectory() as temporary:
            gh("release", "download", tag, "--repo", repo, "--dir", temporary)
            verified_bundle(pathlib.Path(temporary), target, version)
        pending.append(tag)
    # GitHub cannot publish two releases atomically. Re-running finishes a partial pair.
    for tag in pending:
        gh("release", "edit", tag, "--repo", repo, "--draft=false")
    for target in TARGETS:
        gh("release", "view", f"mc{target}-v{version}", "--repo", repo)

if __name__ == "__main__":
    {"prepare": prepare, "package": package, "publish": publish}[sys.argv[1]]()
