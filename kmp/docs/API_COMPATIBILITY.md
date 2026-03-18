# API Compatibility Guardrail (Planned)

## Current State
Binary compatibility validation is **not yet enabled** in this branch because attempted plugin wiring caused KMP configuration breakage in this environment (`KotlinMultiplatformExtension` resolution error during project configuration).

## Blocker
- Need a compatibility-validator plugin/version matrix that is verified against this project's Kotlin plugin setup.

## Temporary Policy
Until automated API checks are wired:
1. Treat public API changes in `lanterna-core-kmp` as review-critical.
2. Require explicit PR notes for public surface changes.
3. Prefer additive changes and avoid silent signature removals/renames.

## Planned Commands (once plugin compatibility is resolved)
```bash
./gradlew -p kmp apiDump --no-daemon
./gradlew -p kmp apiCheck --no-daemon
```
