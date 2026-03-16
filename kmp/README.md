# Lanterna KMP

Kotlin Multiplatform migration workspace for Lanterna, maintained in this codex24 fork.

## Relationship To Original Repository
- Original upstream reference: `mabe02/lanterna`
- This repository: `La-Manufacture-Ambulante/codex24-lanterna-kmp2`
- Goal in this folder: deliver a publishable KMP implementation while preserving Lanterna behavior parity.

## Modules
- `lanterna-core-kmp`: publishable KMP library module.
- `lanterna-demo-kmp`: demo/verification module.

## Local Checks
```bash
./gradlew -p kmp :lanterna-core-kmp:check :lanterna-demo-kmp:check --no-daemon
```

## Documentation (Dokka)
Generate API docs locally:
```bash
./gradlew -p kmp :lanterna-core-kmp:dokkaHtml --no-daemon
```

Publish docs via GitHub Actions workflow:
- `.github/workflows/dokka-pages.yml`

## Publication Baseline
`lanterna-core-kmp` includes:
- `maven-publish`
- `signing` (release-only when keys are present)
- POM metadata (name, license, SCM, developer)

Version/group are centralized in `kmp/build.gradle.kts` using:
- `RELEASE_VERSION` (highest priority)
- `JITPACK_VERSION`
- `VERSION_NAME` from `kmp/gradle.properties`

## Publish To OSSRH
Required environment variables:
- `OSSRH_URL`
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`

Optional signing variables (required for release signing):
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

Publish command:
```bash
./gradlew -p kmp :lanterna-core-kmp:publish --no-daemon
```

Runbook:
- `kmp/docs/RELEASING.md`
