# Lanterna KMP

Kotlin Multiplatform migration workspace for Lanterna.

## Modules
- `lanterna-core-kmp`: publishable KMP library module.
- `lanterna-demo-kmp`: demo/verification module.

## Local Checks
```bash
./gradlew -p kmp :lanterna-core-kmp:check :lanterna-demo-kmp:check --no-daemon
```

## Publication Baseline
`lanterna-core-kmp` includes:
- `maven-publish`
- `signing` (release-only when keys are present)
- POM metadata (name, license, SCM, developer)

Version/group are centralized in `kmp/build.gradle.kts` using:
- `RELEASE_VERSION` (highest priority)
- `JITPACK_VERSION`
- `VERSION_NAME` from `kmp/gradle.properties`

## Publish To OSSRH (when credentials are set)
Required environment variables:
- `OSSRH_URL` (for example: staging or snapshots repo URL)
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`

Optional signing variables (required for release signing):
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

Publish command:
```bash
./gradlew -p kmp :lanterna-core-kmp:publish --no-daemon
```
