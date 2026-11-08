# CI Secrets and Repository Settings

This document defines the minimum GitHub repository configuration required for KMP publication and Dokka Pages deployment.

## 1) Required Actions Secrets (Publish workflow)

Workflow: `.github/workflows/publish.yml`

Required:
- `OSSRH_URL`
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`

Optional for release signing (strongly recommended):
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

## 2) Required Repository Settings (Dokka Pages)

Workflow: `.github/workflows/dokka-pages.yml`

Configure repository settings:
1. Enable GitHub Pages for this repository.
2. Set source to `GitHub Actions`.
3. Ensure Actions permissions allow Pages deployment (`pages:write`, `id-token:write` are already declared in workflow).

## 3) Recommended Branch Protections

For the primary release branch:
- Require status checks to pass before merge.
- Require linear history or squash/rebase policy.
- Restrict force-pushes.

## 4) Verification Commands

Local preflight:
```bash
./kmp/scripts/prepublish_smoke.sh
```

Manual workflow triggers:
- `Publish KMP Artifacts`
- `Publish Dokka Pages`
