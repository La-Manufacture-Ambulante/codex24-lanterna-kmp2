# Contributing

## Scope
This repository contains Java and Kotlin Multiplatform migration work for Lanterna.

## Development Flow
1. Work on the active branch used by the PR stack.
2. Run local checks before pushing:
```bash
./kmp/gradlew -p kmp :lanterna-core-kmp:check :lanterna-demo-kmp:check --no-daemon
```
3. Keep commits focused and descriptive.

## Coding Standards
- Kotlin style: `official` (see `kmp/gradle.properties`).
- Lint must pass (`ktlintCheck`).
- Preserve cross-platform behavior parity where possible.

## Pull Requests
- Explain behavior changes and platform impact.
- Include verification commands and outcomes.
- If a change affects native targets, mention linux/macos/mingw impact explicitly.
