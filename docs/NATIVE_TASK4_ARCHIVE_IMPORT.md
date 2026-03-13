# Native Task 4: Archive Import Candidates

Date: 2026-03-10
Source archive repo: `../lanterna`
Target active repo: `./`

## Goal
Identify native-progress work from the archived `lanterna` repo that can be reused for:
- Task 4: native Linux/macOS
- Task 6 prep: native Windows interop without JNA

## Saved Future Directives (2026-03-10)
- Task 5 scope update:
  - publish both artifacts through JitPack when task 5 is executed:
    - Java library
    - KMP library
- Task 6 scope update:
  - include Mordant repository as additional reference/inspiration for native Windows target design (`mingwX64`, no JNA).

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
3. Current repo previously had JNA files under `src/nativeMain`; this was corrected by moving active JNA backend code to `jvmMain` and replacing `nativeMain` with POSIX/no-JNA helpers.
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

## Implemented Start (2026-03-10)
- JNA backend isolation (JVM):
  - moved Win32/JNA backend from `commonMain` to `jvmMain`:
    - `kmp/lanterna-core-kmp/src/jvmMain/kotlin/com/googlecode/lanterna/terminal/win32/*`
  - result: JNA code now clearly belongs to JVM flavor.
- Native no-JNA bootstrap (Linux/macOS direction):
  - added POSIX runtime helpers in `nativeMain`:
    - `kmp/lanterna-core-kmp/src/nativeMain/kotlin/com/googlecode/lanterna/terminal/nativeposix/PosixTerminalDimensions.kt`
    - `kmp/lanterna-core-kmp/src/nativeMain/kotlin/com/googlecode/lanterna/terminal/nativeposix/PosixTerminalRuntime.kt`
    - `kmp/lanterna-core-kmp/src/nativeMain/kotlin/com/googlecode/lanterna/terminal/nativeposix/PosixTerminalIO.kt`
  - implementation uses `platform.posix` + `kotlinx.cinterop` only (no JNA).

## Implemented Slice 2 (2026-03-10)
- moved JVM-only terminal implementations from `commonMain` to `jvmMain`:
  - `kmp/lanterna-core-kmp/src/jvmMain/kotlin/com/googlecode/lanterna/terminal/DefaultTerminalFactory.kt`
  - `kmp/lanterna-core-kmp/src/jvmMain/kotlin/com/googlecode/lanterna/terminal/ansi/*`
  - `kmp/lanterna-core-kmp/src/jvmMain/kotlin/com/googlecode/lanterna/terminal/swing/*`
- outcome:
  - JVM checks remain green.
  - `commonMain` no longer contains `terminal/ansi` or `terminal/swing` packages.
  - remaining `commonMain` JVM import footprint is still significant (`84` files with `java.*`/`javax.*`/`com.sun.*` imports), so native target enablement still requires further slicing.

## Current Constraint
- `lanterna-core-kmp` still contains broad JVM/desktop APIs in `commonMain` (java/io/awt/swing, reflection).
- Because of this, enabling macOS/Linux targets for this module immediately would fail compilation.
- Next implementation step for true native builds:
  - split JVM-only packages from `commonMain` into `jvmMain` in bounded slices, then enable linux/macos targets.

## Implemented Slice 3 (2026-03-10)
- completed the remaining `commonMain -> jvmMain` move for converted Lanterna JVM-centric sources.
- enabled native targets in `kmp/lanterna-core-kmp/build.gradle.kts`:
  - `linuxX64()`
  - `macosX64()`
  - `macosArm64()`
- kept JNA dependencies JVM-scoped in `jvmMain`.
- set `kotlin.native.ignoreDisabledTargets=true` in `kmp/gradle.properties` to keep Linux-host output clean while macOS targets remain declared for macOS CI runners.
- validation:
  - `:lanterna-core-kmp:compileKotlinLinuxX64`
  - `:lanterna-core-kmp:jvmTest`
  - `:lanterna-core-kmp:check`
  - `:lanterna-demo-kmp:check`
  - all passed on Linux host.

## Updated Constraint
- `lanterna-core-kmp` no longer has a broad JVM API surface in `commonMain` because the remaining converted sources were moved under `jvmMain`.
- native targets compile for Linux host, while macOS targets are declared and skipped on Linux (to be validated in macOS CI).
- next step is adding a minimal shared/native-facing API layer that can be consumed by linux/macos implementations without depending on JVM-only classes.

## Deferred Follow-Up Task (2026-03-13)
- PR4 comment restoration sweep:
  - Problem: several Kotlin files in PR4 lost Java/KDoc/in-code explanatory comments during conversion/merge slices.
  - Task: run a targeted parity pass to restore missing comments (ordering and content aligned with Java source where applicable) without changing runtime behavior.
  - Scope note: schedule after native parity validation and CI stabilization slices are complete.
