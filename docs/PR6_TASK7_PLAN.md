# PR6 Plan: Task 6 Partial + Task 7 Coroutines

Date: 2026-03-13
Branch: `codex/pr6-coroutines-impl`

## Task Status Update
- Task 6 (native Windows interop without JNA): **partially done**
- Current state kept from PR5: enabled `mingwX64` target, Win32 runtime/input/output scaffolding, key/mouse translation slices, and compile coverage on non-Windows hosts.
- Remaining for full completion:
  - run and validate on real Windows CI runner,
  - complete parity edge-case mapping and integration checks,
  - add/expand Windows-specific parity tests.

## Execution Plan (Task 7)
1. Slice A: add coroutine dependency and concurrency abstraction entry points in `commonMain` while keeping thread mode as default.
2. Slice B: implement coroutine-backed launch path in abstraction, with no callsite migration yet.
3. Slice C: add tests for mode selection and coroutine execution behavior.
4. Slice D: validate compile/test gates on JVM + native compiles.
5. Slice E: prepare follow-up migration plan for selective callsite adoption.

## Progress
- [x] Slice A completed: coroutine dependency + `PlatformTaskRuntime` abstraction + baseline tests.
- [x] Lint propagation from PR5 applied on PR6 (`PosixTerminalIO` / `PosixTerminalRuntime` style fixes).
- [x] Slice B completed: first callsite adoption wired (`SeparateTextGUIThread` launches through `PlatformTaskRuntime`).
- [x] Slice C completed: added GUI-thread coroutine-mode regression test for `SeparateTextGUIThread` lifecycle.
- [x] Slice D completed (local gate): `ktlintCheck`, `compileKotlinJvm`, `compileKotlinLinuxX64`, `compileKotlinMingwX64`, and `jvmTest` all passing.
- [x] Slice E (part 1) completed: env-policy/runtime hardening in `PlatformTaskRuntime` with deterministic tests for
  - env-based mode reconfiguration via injectable environment lookup,
  - await-timeout semantics for running tasks.
- [x] Slice E (part 2) completed: runtime selection policy now checks JVM system property override first
  (`lanterna.execution.mode`) and falls back to environment variable (`LANTERNA_EXECUTION_MODE`) for cross-platform
  use; deterministic precedence tests added in `PlatformTaskRuntimeTest`.
- [x] Slice E (part 3) completed: GUI-thread launch path now refreshes runtime mode from policy at `start()` and
  regression tests cover environment-driven coroutine selection plus property-over-environment precedence in
  `SeparateTextGUIThreadCoroutineModeTest`.
- [ ] Slice E (part 4) next: migrate one additional non-GUI polling/backoff callsite behind runtime policy hooks.

## Guardrails
- Default behavior remains thread-based.
- No functional switch at callsites in this PR slice.
- Keep PR6 scoped to plan save + Task 7 foundation.

## Follow-up Callsite Plan (post-PR6)
1. Evaluate `InputDecoder` backoff waits (`sleepCurrentThread`) for optional coroutine delay path behind
   `PlatformTaskRuntime` policy hooks.
2. Audit GUI polling loops in `MultiWindowTextGUI` for cooperative coroutine scheduling opportunities while preserving
   existing semantics.
3. Add parity tests for each migrated callsite ensuring default thread behavior remains unchanged when no runtime mode
   override is set.
