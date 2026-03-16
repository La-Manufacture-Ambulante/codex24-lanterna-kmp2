# KMP Releasing Runbook

## Prerequisites
- CI checks green for stacked PR heads.
- Publication env vars available:
  - `RELEASE_VERSION`
  - `OSSRH_URL`
  - `OSSRH_USERNAME`
  - `OSSRH_PASSWORD`
  - `SIGNING_KEY`
  - `SIGNING_PASSWORD`

## 1. Verify build
```bash
./gradlew -p kmp :lanterna-core-kmp:check :lanterna-demo-kmp:check --no-daemon
```

## 2. Verify publish path (dry/local)
```bash
./gradlew -p kmp :lanterna-core-kmp:publishJvmPublicationToMavenLocal --no-daemon
```

## 3. Publish
```bash
RELEASE_VERSION=<version> \
OSSRH_URL=<repo-url> \
OSSRH_USERNAME=<user> \
OSSRH_PASSWORD=<pass> \
SIGNING_KEY="<ascii-armored-key>" \
SIGNING_PASSWORD=<key-pass> \
./gradlew -p kmp :lanterna-core-kmp:publish --no-daemon
```

## 4. Post-publish verification
- Confirm artifacts are present in the target repository.
- Validate POM metadata (`name`, `license`, `scm`, `developer`).
- Smoke-consume published coordinates in a sample project.

## Notes
- Full `publishToMavenLocal` currently exercises native metadata and may fail due unrelated pre-existing native metadata issues. Use JVM publish task as a baseline check while that issue is open.
