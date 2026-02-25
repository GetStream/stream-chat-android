# Roadmap: Release Process Migration

**Created:** 2026-02-04
**Milestone:** v1.0 — Release Process Migration

## Overview

| Phase | Name | Goal | Requirements |
|-------|------|------|--------------|
| 1 | Create Labels | Create all 11 pr:* labels via GitHub API | LBL-01 to LBL-11 |
| 2 | Add Config Files | Add release.yaml, pr-quality.yml, update PR template | CFG-01, CFG-02, TPL-01 |

## Phase Details

### Phase 1: Create Labels

**Goal:** Create all 11 `pr:*` labels in stream-chat-android repo matching video/core

**Requirements:** LBL-01 through LBL-11

**Success Criteria:**
1. All 11 labels exist in repo with correct names
2. All labels have correct colors matching core
3. All labels have descriptions matching core

**Implementation Notes:**
```bash
# Create labels via gh CLI
gh label create "pr:breaking-change" --color "b60205" --description "Breaking change" --repo GetStream/stream-chat-android
gh label create "pr:new-feature" --color "a2eeef" --description "New feature" --repo GetStream/stream-chat-android
gh label create "pr:bug" --color "d73a4a" --description "Bug fix" --repo GetStream/stream-chat-android
gh label create "pr:improvement" --color "0e8a16" --description "Improvement" --repo GetStream/stream-chat-android
gh label create "pr:documentation" --color "0075ca" --description "Documentation" --repo GetStream/stream-chat-android
gh label create "pr:dependencies" --color "0366d6" --description "Dependency updates" --repo GetStream/stream-chat-android
gh label create "pr:internal" --color "cfd3d7" --description "Internal changes / housekeeping" --repo GetStream/stream-chat-android
gh label create "pr:ci" --color "5319e7" --description "CI changes" --repo GetStream/stream-chat-android
gh label create "pr:test" --color "d4c5f9" --description "Test-only changes" --repo GetStream/stream-chat-android
gh label create "pr:demo-app" --color "fbca04" --description "Changes specific to demo app" --repo GetStream/stream-chat-android
gh label create "pr:ignore-for-release" --color "999999" --description "Exclude from release notes" --repo GetStream/stream-chat-android
```

---

### Phase 2: Add Config Files

**Goal:** Add release.yaml, pr-quality.yml workflow, and update PR template

**Requirements:** CFG-01, CFG-02, TPL-01

**Success Criteria:**
1. `.github/release.yaml` exists and matches video/core
2. `.github/workflows/pr-quality.yml` exists and uses conventions
3. `.github/pull_request_template.md` updated to video style
4. PR created and merged

**Files to Create/Update:**

**`.github/release.yaml`** (copy from video):
```yaml
changelog:
  exclude:
    labels:
      - "pr:ignore-for-release"
    authors:
      - "github-actions[bot]"
      - "stream-public-bot"
  categories:
    - title: Breaking Changes 🛠
      labels:
        - "pr:breaking-change"
    - title: New Features 🎉
      labels:
        - "pr:new-feature"
    - title: Bug Fixes 🐛
      labels:
        - "pr:bug"
    - title: Improvements ✨
      labels:
        - "pr:improvement"
    - title: Documentation 📚
      labels:
        - "pr:documentation"
    - title: Dependencies 📦
      labels:
        - "pr:dependencies"
    - title: Internal 🧪
      labels:
        - "pr:internal"
        - "pr:ci"
        - "pr:test"
    - title: Demo App 🧩
      labels:
        - "pr:demo-app"
    - title: Other Changes
      labels:
        - "*"
```

**`.github/workflows/pr-quality.yml`**:
```yaml
name: PR checklist

on:
  pull_request:
    types: [ opened, edited, synchronize, labeled, unlabeled, reopened ]

permissions:
  contents: read
  pull-requests: write
  issues: write

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  pr-checklist:
    uses: GetStream/stream-build-conventions-android/.github/workflows/pr-quality.yml@v0.7.1
    secrets: inherit
```

---

## Progress

- [x] Phase 1: Create Labels ✓
- [x] Phase 2: Add Config Files ✓ (PR #6127)

---
*Roadmap created: 2026-02-04*
