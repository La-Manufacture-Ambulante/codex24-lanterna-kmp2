# API Compatibility Guardrail

Target policy:
- Gate published API changes in CI before release.

Current status:
- Automated API compatibility checks are not yet enabled on this branch.

Planned automation:
1. Add binary compatibility validator once a plugin/toolchain combination is confirmed compatible with Kotlin `2.1.21` + this KMP layout.
2. Commit API baseline dumps.
3. Add `apiCheck` to required CI gates.

Interim policy:
- Treat public API changes in `lanterna-core-kmp` as release-impacting.
- Require explicit reviewer acknowledgment for signature/visibility changes.
