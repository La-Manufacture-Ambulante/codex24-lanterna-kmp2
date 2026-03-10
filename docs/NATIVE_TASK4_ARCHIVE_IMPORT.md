# Native Task 4: Archive Import Candidates

Date: 2026-03-10
Source archive repo: `../lanterna`
Target active repo: `./`

## Goal
Identify native-progress work from the archived `lanterna` repo that can be reused for:
- Task 4: native Linux/macOS
- Task 6 prep: native Windows interop without JNA

## What Was Checked
- `lanterna/kmp/lanterna-core-kmp/build.gradle.kts`
- `lanterna/kmp/lanterna-demo-kmp/build.gradle.kts`
- `lanterna/kmp/lanterna-core-kmp/src/nativeMain/**`
- `lanterna/kmp/lanterna-demo-kmp/src/nativeMain/**`
- `lanterna/kmp/docs/RESEARCH_TRUE_KMP_WINDOWS_PATH.md`
- Current equivalents in `lanterna-mabe02`

## Key Findings
1. Archive has native targets enabled in Gradle (`macosArm64`, `macosX64`, `linuxX64`, `linuxArm64`, `mingwX64`); current repo only has `jvm()`.
2. Archive contains true Kotlin/Native runtime code using `kotlinx.cinterop` and `platform.posix`.
3. Current repo contains `src/nativeMain` files that still depend on JVM-only JNA (`com.sun.jna.*`), so they will not compile as real Kotlin/Native.
4. Archive includes a focused Windows-no-JNA research doc that is directly reusable for Task 6 planning.

## Copy Candidates

### A) High-confidence direct copy (docs/planning)
- Source: `lanterna/kmp/docs/RESEARCH_TRUE_KMP_WINDOWS_PATH.md`
- Target suggestion: `lanterna-mabe02/docs/RESEARCH_TRUE_KMP_WINDOWS_PATH.md`
- Why: still relevant and aligned with current task list item 6.

### B) Build config pattern to adapt (not direct copy)
- Source: `lanterna/kmp/lanterna-core-kmp/build.gradle.kts`
- Target: `lanterna-mabe02/kmp/lanterna-core-kmp/build.gradle.kts`
- Reuse:
  - Native target declarations:
    - `macosArm64()`
    - `macosX64()`
    - `linuxX64()`
    - `linuxArm64()`
    - `mingwX64()`
- Note: keep existing JaCoCo/report setup in current file; merge instead of replace.

### C) Demo native executable pattern to adapt
- Source: `lanterna/kmp/lanterna-demo-kmp/build.gradle.kts`
- Target: `lanterna-mabe02/kmp/lanterna-demo-kmp/build.gradle.kts`
- Reuse:
  - Native target declarations
  - `binaries { executable(...) }` structure for native smoke executable(s)
- Note: current repo does not yet have `demo nativeMain` entrypoints; add minimal one first.

### D) Native implementation pattern (reference-level reuse)
- Source files:
  - `lanterna/kmp/lanterna-core-kmp/src/nativeMain/kotlin/com/googlecode/lanterna/kmp/core/TerminalRuntime.native.kt`
  - `lanterna/kmp/lanterna-core-kmp/src/nativeMain/kotlin/com/googlecode/lanterna/kmp/core/TerminalIO.native.kt`
- Target area:
  - `lanterna-mabe02/kmp/lanterna-core-kmp/src/nativeMain/kotlin/...`
- Reuse:
  - `platform.posix` and `kotlinx.cinterop` approach for size/input/output lifecycle
- Note: package/API differs (`...kmp.core` vs converted Lanterna classes), so this is a design pattern, not copy-paste code.

## Not Safe To Copy Directly
- Archive `.../kmp/core/*` native files as-is into current codebase (API mismatch).
- Current `lanterna-mabe02` native files using `com.sun.jna.*` into real K/N targets (will fail for non-JVM native).

## Recommended Import Sequence
1. Copy Windows research doc (A) for immediate task continuity.
2. Add native targets into both KMP module build files (B, C), keeping current CI/coverage logic.
3. Add one tiny native demo entrypoint for macOS/Linux smoke build.
4. Replace JNA-based `nativeMain` path with true K/N interop path (D) behind minimal abstractions.

## Validation Commands (after applying B/C/D)
- `gradle -p kmp :lanterna-core-kmp:compileKotlinMacosArm64`
- `gradle -p kmp :lanterna-core-kmp:compileKotlinLinuxX64`
- `gradle -p kmp :lanterna-demo-kmp:linkDebugExecutableMacosArm64`

