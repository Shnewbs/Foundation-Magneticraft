import hashlib
import importlib.util
import json
import os
import pathlib
import tempfile
import types
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("releases", pathlib.Path(__file__).with_name("releases.py"))
releases = importlib.util.module_from_spec(spec)
spec.loader.exec_module(releases)

class ReleaseTests(unittest.TestCase):
    def test_version_validation(self):
        for version in ("0.0.1a", "0.0.1a.R2", "1.0.0", "1.2.3b"):
            self.assertEqual(releases.checked_version(version), version)
        for version in ("", "../tag", "1.0.0\n", "$(secret)", "1.0"):
            with self.assertRaises(ValueError):
                releases.checked_version(version)

    def test_mismatched_branches_wait_without_publication(self):
        versions = iter(("0.0.2a", "0.0.1a"))
        def fake_gh(*args, **kwargs):
            target = "1.21.1" if "1.21.1" in args[1] else "26.3"
            return types.SimpleNamespace(stdout=f"mod_version={next(versions)}\nminecraft_version={target}\n")
        # Ref requests return distinguishable frozen SHAs; properties are read at those SHAs.
        shas = iter(("1.21.1", "26.3"))
        with tempfile.TemporaryDirectory() as temporary:
            output = pathlib.Path(temporary) / "output"
            with patch.dict(os.environ, {"GH_REPO": "owner/repo", "GITHUB_OUTPUT": str(output)}):
                with patch.object(releases, "api", side_effect=lambda _: {"object": {"sha": next(shas)}}):
                    with patch.object(releases, "gh", side_effect=fake_gh):
                        releases.prepare()
            self.assertEqual(output.read_text(), "ready=false\n")

    def test_wrong_target_and_corrupted_artifact_are_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            name = "Foundations-Magneticraft-1.21.1-0.0.1a.jar"
            (directory / name).write_bytes(b"jar")
            manifest = {"target": "1.21.1", "version": "0.0.1a", "artifact": name,
                        "source_sha": "a" * 40, "sha256": hashlib.sha256(b"jar").hexdigest()}
            (directory / "release-manifest.json").write_text(json.dumps(manifest))
            releases.verified_bundle(directory, "1.21.1", "0.0.1a")
            with self.assertRaises(ValueError):
                releases.verified_bundle(directory, "26.3", "0.0.1a")
            (directory / name).write_bytes(b"modified")
            with self.assertRaises(ValueError):
                releases.verified_bundle(directory, "1.21.1", "0.0.1a")

if __name__ == "__main__":
    unittest.main()
