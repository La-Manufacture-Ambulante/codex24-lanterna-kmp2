# Releasing `lanterna-core-kmp`

## Prerequisites
- JDK 17+
- Gradle wrapper available (`kmp/gradlew`)
- OSSRH target URL and credentials
- Signing key/password for release signatures

## Required Environment Variables
- `OSSRH_URL`
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`
- `SIGNING_KEY` (release)
- `SIGNING_PASSWORD` (release)
- `RELEASE_VERSION` (optional but recommended for release builds)

## Local Verification
```bash
./kmp/gradlew -p kmp :lanterna-core-kmp:check --no-daemon
./kmp/gradlew -p kmp :lanterna-core-kmp:dokkaHtml --no-daemon
./kmp/gradlew -p kmp :lanterna-core-kmp:publishToMavenLocal --no-daemon
```

## CI Publication
Use GitHub Actions workflow `Publish KMP Artifacts` and set:
- `release_version` input (optional)
- repository secrets (`OSSRH_*`, `SIGNING_*`)
- repository setup per `kmp/docs/CI_SECRETS_AND_SETTINGS.md`

## Post-Publish Verification
- Confirm published coordinates in target repository.
- Validate generated POM metadata (SCM, license, developer).
- Smoke consume artifact from a fresh sample project.
