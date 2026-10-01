# Automatic paired releases

A push changing `gradle.properties` on either `1.21.1` or `26.3` starts the paired release workflow.

1. Update `mod_version` and `PORT_STATUS.md` / release notes on BOTH branches.
2. The first branch waits if versions differ. The second matching update builds both frozen source commits.
3. Both builds must pass. Each JAR is checked for metadata, license and the mod entry point.
4. Upload and verify both drafts before publishing separate target releases.
5. Alpha/beta suffixes create prereleases; revisions use `.R2`, `.R3`, etc.
6. Existing published files and tags are never overwritten. Changing a released version requires a revision.
7. If publishing is interrupted, rerun the workflow to finish the pair.

Use Actions > Paired releases > Run workflow for manual recovery.
The manual default builds/validates only; select Publish for a recovery/publication run.
Push-triggered matching versions publish automatically.

Names:
- `Foundations-Magneticraft-1.21.1-<version>.jar`, tag `mc1.21.1-v<version>`
- `Foundations-Magneticraft-26.3-<version>.jar`, tag `mc26.3-v<version>`

Each release includes its JAR, SHA256SUMS.txt, release-manifest.json, and notes with exact source/toolchain versions and known limitations.
These checks do not replace Minecraft client/server runtime testing. Record runtime results accurately in PORT_STATUS.md before bumping versions.
The current bootstrap remains unreleased; installing this workflow does not trigger publication.

Requires GitHub Actions enabled and its workflow token permitted to write repository contents. No personal access token is needed.
