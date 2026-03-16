# KMP Prepublication Checklist

Use this checklist before cutting a release or declaring publication-ready state.

## 1. Branch and CI health
- [ ] Target branch is up to date with its intended stack base.
- [ ] Required CI checks are green on the PR head commit.
- [ ] No duplicated/obsolete workflow triggers creating redundant runs.

## 2. Artifact integrity
- [ ] `:lanterna-core-kmp:publishToMavenLocal` succeeds.
- [ ] Published modules include expected targets for this branch.
- [ ] JitPack (if used for branch validation) reports `status: ok` for head version.

## 3. Metadata and signing
- [ ] POM metadata fields are present and correct.
- [ ] Group/version are set via release policy variables.
- [ ] Signing is enabled for release publication with valid signing keys.

## 4. Documentation
- [ ] `README.md` explains fork positioning and current publication status.
- [ ] `kmp/README.md` documents module/target commands and variables.
- [ ] Dokka workflow is present and executable.
- [ ] Release runbook is current (`kmp/docs/RELEASING.md`).

## 5. Compatibility and governance
- [ ] API compatibility strategy is documented and enforced where possible.
- [ ] Dependency update automation is active (`dependabot`/equivalent).
- [ ] Gradle wrapper validation is active in CI.

## 6. Release execution
- [ ] Release version/tag selected and recorded.
- [ ] Publish workflow executed with required secrets.
- [ ] Artifact coordinates verified by consumer sample or dependency resolution test.

## 7. Post-release checks
- [ ] Release notes/changelog published.
- [ ] Docs links point to current API docs.
- [ ] Follow-up issues captured for known limitations.
