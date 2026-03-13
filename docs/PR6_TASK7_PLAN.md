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

## Guardrails
- Default behavior remains thread-based.
- No functional switch at callsites in this PR slice.
- Keep PR6 scoped to plan save + Task 7 foundation.
