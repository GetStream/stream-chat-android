# Stream Chat Android — Release Process Migration

## What This Is

Migrating stream-chat-android's release process to match stream-video-android and stream-core-android. Currently chat uses manual CHANGELOG.md updates; target is label-based automatic release notes via GitHub.

## Core Value

Consistent release process across all Stream Android SDKs — label PRs, GitHub generates release notes automatically.

## Requirements

### Validated

(None yet — first milestone)

### Active

- [ ] Create 11 `pr:*` labels matching video/core
- [ ] Add `release.yaml` for GitHub release notes
- [ ] Add `pr-quality.yml` workflow for label enforcement
- [ ] Update PR template to match video/core style
- [ ] Keep `CHANGELOG.md` for history (no deletion)

### Out of Scope

- Backfilling labels on existing PRs — too much manual work, not needed
- Changing existing release workflow scripts — only adding new config
- Removing CHANGELOG.md — keep for history

## Context

**Current state:**
- Chat uses manual CHANGELOG.md with per-module sections
- No `pr:*` labels exist
- No label enforcement workflow
- PR template requires "Changelog is updated" checkbox

**Target state (video/core):**
- `release.yaml` generates GitHub release notes from labels
- `pr-quality.yml` workflow enforces labels (via `stream-build-conventions-android`)
- 11 labels: `pr:breaking-change`, `pr:bug`, `pr:ci`, `pr:demo-app`, `pr:dependencies`, `pr:documentation`, `pr:ignore-for-release`, `pr:improvement`, `pr:internal`, `pr:new-feature`, `pr:test`

## Constraints

- **Conventions version**: Use `v0.7.1` (already used for android-ci.yml)
- **No code changes**: This is purely CI/config
- **Backward compatible**: CHANGELOG.md stays for history

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use conventions v0.7.1 | Already used in chat repo | — Pending |
| Keep CHANGELOG.md | History preservation | — Pending |
| Copy video PR template | More detailed than core | — Pending |

---
*Last updated: 2026-02-04 after project initialization*
