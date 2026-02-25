# Requirements: Release Process Migration

**Defined:** 2026-02-04
**Core Value:** Consistent release process across all Stream Android SDKs

## v1 Requirements

### Labels

- [x] **LBL-01**: Create `pr:breaking-change` label (color: b60205)
- [x] **LBL-02**: Create `pr:new-feature` label (color: a2eeef)
- [x] **LBL-03**: Create `pr:bug` label (color: d73a4a)
- [x] **LBL-04**: Create `pr:improvement` label (color: 0e8a16)
- [x] **LBL-05**: Create `pr:documentation` label (color: 0075ca)
- [x] **LBL-06**: Create `pr:dependencies` label (color: 0366d6)
- [x] **LBL-07**: Create `pr:internal` label (color: cfd3d7)
- [x] **LBL-08**: Create `pr:ci` label (color: 5319e7)
- [x] **LBL-09**: Create `pr:test` label (color: d4c5f9)
- [x] **LBL-10**: Create `pr:demo-app` label (color: fbca04)
- [x] **LBL-11**: Create `pr:ignore-for-release` label (color: 999999)

### GitHub Config

- [x] **CFG-01**: Add `.github/release.yaml` matching video/core
- [x] **CFG-02**: Add `.github/workflows/pr-quality.yml` using conventions

### PR Template

- [x] **TPL-01**: Update `.github/pull_request_template.md` to match video style

## Out of Scope

| Feature | Reason |
|---------|--------|
| Backfill labels on old PRs | Too much manual work, unnecessary |
| Remove CHANGELOG.md | Keep for history |
| Change release workflow | Only adding config, not changing automation |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| LBL-01 | Phase 1 | ✓ Complete |
| LBL-02 | Phase 1 | ✓ Complete |
| LBL-03 | Phase 1 | ✓ Complete |
| LBL-04 | Phase 1 | ✓ Complete |
| LBL-05 | Phase 1 | ✓ Complete |
| LBL-06 | Phase 1 | ✓ Complete |
| LBL-07 | Phase 1 | ✓ Complete |
| LBL-08 | Phase 1 | ✓ Complete |
| LBL-09 | Phase 1 | ✓ Complete |
| LBL-10 | Phase 1 | ✓ Complete |
| LBL-11 | Phase 1 | ✓ Complete |
| CFG-01 | Phase 2 | ✓ Complete |
| CFG-02 | Phase 2 | ✓ Complete |
| TPL-01 | Phase 2 | ✓ Complete |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0 ✓

---
*Requirements defined: 2026-02-04*
