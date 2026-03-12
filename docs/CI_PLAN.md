# CI Plan (PR3)

## Goals
- Keep Java library compatibility healthy (`mvn test`).
- Validate KMP JVM build/tests across Linux/macOS/Windows.
- Add Kotlin lint/static analysis with incremental strictness.
- Produce and publish coverage artifacts for review.

## Workflows
1. `ci.yml` (blocking)
   - Java library test path on Ubuntu (`mvn -B -ntp test`).
   - KMP JVM checks on Ubuntu/macOS/Windows with Gradle 8.7.

2. `lint.yml` (mixed)
   - `ktlint` formatting/style check on `kmp/**/*.kt` (advisory first, non-blocking).
   - `detekt` static analysis report (advisory first, non-blocking).

3. `coverage.yml` (reporting)
   - KMP JVM tests + JaCoCo report generation (`:lanterna-core-kmp:jvmTestCoverageReport`).
   - Upload coverage artifacts (`kmp/lanterna-core-kmp/build/reports/jacoco/jvmTestCoverageReport/**`).

## Rollout
1. Start with tests/build as required gates.
2. Keep ktlint and detekt non-blocking while conversion stabilizes.
3. Once findings are reduced, move ktlint first, then detekt, to blocking.

## Notes
- Native target CI (Linux/macOS/Windows Kotlin/Native) is a follow-up after native targets are fully wired in Gradle.
