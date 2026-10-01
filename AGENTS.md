# Foundations Magneticraft development instructions

- Treat `1.21.1` as the primary development branch.
- Maintain `26.3` alongside it and prepare for the later 26.4 API migration.
- Commit and push completed changes to GitHub during each authorized work session without asking again for routine pushes.
- Implement applicable changes on both target branches and run their appropriate checks.
- Do not merge target-specific platform code wholesale between branches.
- Keep matching mod versions for paired automatic releases.
- Update PORT_STATUS.md with accurate implementation and verification results before version bumps.
- Do not claim client/server runtime verification from a compilation result.
- Preserve `1.12` as the upstream reference branch.
- Do not store credentials in source.
