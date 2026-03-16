# Research: True KMP Windows Path (No JVM/JNA)

Date: 2026-02-27
Scope: Assess feasibility of a real Kotlin Multiplatform Windows backend (`mingwX64`) for Lanterna-style terminal behavior, without relying on JVM/JNA.

## Executive Summary
- A true KMP Windows path is feasible.
- JNA is not a viable option for `mingwX64`; it is JVM/Java-oriented.
- The realistic implementation paths are:
  1. Direct Win32 console APIs via Kotlin/Native platform libraries + cinterop.
  2. Wrapping a C terminal library (for example PDCurses) through cinterop.
- Difficulty is medium-high due to console mode/input/event details and behavior parity work.

## What Is Available Today
- Kotlin/Native supports `mingwX64` target (Tier 3), so a Windows native target is valid.
- Kotlin/Native has first-class C interop and bundled platform libraries for native APIs.
- Microsoft console APIs provide the primitives needed for a Lanterna-like backend (`SetConsoleMode`, `ReadConsoleInput`, virtual terminal behavior guidance).

## Why JNA Is Not the Native-KMP Solution
- Lanterna's Java Windows implementation uses JNA in `terminal/win32`.
- JNA is designed for Java runtime usage, so it fits JVM-on-Windows, not Kotlin/Native `mingwX64`.

## Candidate Approaches

### 1) Direct Win32 backend (recommended long-term)
- Use Kotlin/Native + Win32 calls for:
  - console mode setup/restore,
  - input events,
  - resize/viewport handling,
  - output and VT sequence behavior.
- Pros:
  - No extra C dependency.
  - Most control over behavior parity.
- Cons:
  - More engineering effort.
  - More platform edge-case handling.

### 2) PDCurses wrapper via cinterop
- Bind to PDCurses API through cinterop and adapt to KMP contracts.
- Pros:
  - Reuses mature terminal abstractions.
- Cons:
  - Added native dependency management.
  - Potential impedance mismatch with Lanterna-style API and rendering model.

## Difficulty Estimate
- Direct Win32 K/N backend: medium-high.
- PDCurses cinterop backend: medium-high (different risks, not necessarily less total work).

Primary complexity drivers:
- Correct console mode transitions and restoration.
- Input event decoding parity (keys, modifiers, mouse, resize).
- UTF/codepage behavior and VT compatibility.
- Matching behavior with existing JVM/JS/macOS paths.

## Practical Recommendation
- Keep JVM-on-Windows support separate (JNA-compatible path if needed there).
- Build true `mingwX64` backend with direct Win32 APIs for long-term KMP correctness.
- Treat it as a dedicated milestone with explicit parity tests (input, resize, output, lifecycle).

## Minimal-First Execution Plan

### M1: Minimal native Windows backend (first increment)
- Goal: a bounded backend that proves end-to-end native viability.
- Scope:
  - terminal mode open/restore,
  - basic output (`putCharacter`, `putString`, `flush`),
  - terminal size query,
  - minimal input mapping (characters + arrows + Enter/Esc),
  - smoke executable path.
- Estimated effort: low-to-medium (roughly days, not weeks) if kept strict.

### M2: Parity-hardening increment
- Goal: move from viable to migration-grade behavior parity.
- Scope:
  - richer SGR/color behavior,
  - mouse/resize semantics parity,
  - cursor/private-mode edge behavior,
  - deeper compatibility tests and failure-mode hardening.
- Estimated effort: medium-high (multi-week risk band, depending on parity bar and edge cases).

## Validation Strategy Without a Local Windows Machine
- What can be validated off-Windows:
  - source-level architecture and separation of Win32 calls behind adapters,
  - deterministic unit tests for decode/state transitions using fake inputs,
  - compile/link checks on Windows-capable CI.
- What cannot be fully validated off-Windows:
  - real console behavior under Windows terminal hosts,
  - mode-switch and input-event edge behavior in actual runtime.
- Practical implication:
  - local non-Windows work can reduce risk substantially, but final confidence requires at least one Windows runtime lane (CI and/or manual).

## Local Windows VM Options (for manual verification)
- Parallels Desktop:
  - easiest setup / best UX on macOS,
  - typically uses Windows 11 ARM on Apple Silicon.
- UTM:
  - free/open option,
  - generally slower, more setup friction.
- VMware Fusion:
  - viable depending on host architecture and guest support constraints.

## CI Direction Status
- GitHub Actions Windows runner path is recognized as the most reliable verification baseline for native Windows behavior.
- Decision to implement CI workflow is explicitly deferred for now (pending user direction).

## Sources
- Kotlin/Native target support (`mingwX64`): https://kotlinlang.org/docs/native-target-support.html
- Kotlin/Native C interop: https://kotlinlang.org/docs/native-c-interop.html
- Kotlin/Native platform libraries: https://kotlinlang.org/docs/native-platform-libs.html
- Microsoft `SetConsoleMode`: https://learn.microsoft.com/en-us/windows/console/setconsolemode
- Microsoft `ReadConsoleInput`: https://learn.microsoft.com/en-us/windows/console/readconsoleinput
- Microsoft classic console vs VT guidance: https://learn.microsoft.com/en-us/windows/console/classic-vs-vt
- Microsoft ConPTY API: https://learn.microsoft.com/en-us/windows/console/createpseudoconsole
- PDCurses overview: https://pdcurses.org/
- PDCurses Windows console notes: https://pdcurses.org/wincon/
- JNA project overview: https://github.com/java-native-access/jna
- Lanterna README (pure Java statement): https://github.com/mabe02/lanterna
- Lanterna local JNA usage evidence: `src/main/java/com/googlecode/lanterna/terminal/win32/Wincon.java`, `src/main/java/com/googlecode/lanterna/terminal/win32/WindowsTerminal.java`
- Kotter native Windows reference implementation (for comparison): `https://github.com/varabyte/kotter/blob/main/kotter/src/winMain/kotlin/com/varabyte/kotter/terminal/native/NativeTerminal.kt`
