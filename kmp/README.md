# Lanterna KMP

Kotlin Multiplatform migration workspace for Lanterna in the codex24 fork.

## Repository relationship
- Upstream reference: `mabe02/lanterna`
- KMP migration/publishing fork: `La-Manufacture-Ambulante/codex24-lanterna-kmp2`
- This directory owns KMP targets, publication config, and KMP-specific CI/docs.

## Modules
- `lanterna-core-kmp`: publishable KMP library module.
- `lanterna-demo-kmp`: demo and behavior verification module.

## Platform status
- `jvm`: primary supported target
- `linuxX64`: native parity + publication target
- `macosX64`: native parity + publication target
- `macosArm64`: native parity + publication target
- `mingwX64`: parity track in progress (branch dependent)

## Local verification
```bash
./gradlew -p kmp :lanterna-core-kmp:check :lanterna-demo-kmp:check --no-daemon
./gradlew -p kmp :lanterna-core-kmp:publishToMavenLocal --no-daemon
```

## Documentation (Dokka)
Generate API docs locally:
```bash
./gradlew -p kmp :lanterna-core-kmp:dokkaHtml --no-daemon
```

Publish docs via GitHub Pages workflow:
- `.github/workflows/dokka-pages.yml`

## Publication baseline
`lanterna-core-kmp` is configured with:
- `maven-publish`
- `signing` (release signing required when keys are provided)
- POM metadata (license, SCM, developer)

Version/group resolution in `kmp/build.gradle.kts`:
1. `RELEASE_VERSION`
2. `JITPACK_VERSION`
3. `VERSION_NAME` (from `kmp/gradle.properties`)

## Publish to OSSRH
Required variables:
- `OSSRH_URL`
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`

Signing variables (for release signing):
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

Command:
```bash
./gradlew -p kmp :lanterna-core-kmp:publish --no-daemon
```

## Prepublication docs
- Runbook: `kmp/docs/RELEASING.md`
- API policy: `kmp/docs/API_COMPATIBILITY.md`
- Operational checklist: `kmp/docs/PREPUBLICATION_CHECKLIST.md`
