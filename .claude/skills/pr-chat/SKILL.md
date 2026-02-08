---
name: pr-chat
description: Automate git workflow for stream-chat-android - create branch, commit, push, and open draft PR
---

# PR Chat - Git Workflow Automation

Automate the full git workflow: branch creation, commits, push, and draft PR creation for the stream-chat-android project.

## Usage

```
/pr-chat <branch-name> [commit-message] [base-branch]
```

**Arguments:**
- `branch-name` (required): The feature branch to create/use
- `commit-message` (optional): The commit message. If not provided, analyze the changes and generate an appropriate message.
- `base-branch` (optional): The target branch for the PR. Defaults to `develop` if not specified.

**Examples:**
```
/pr-chat feature/add-typing-indicators
/pr-chat feature/add-typing-indicators "Add typing indicators to message list"
/pr-chat bug/message-sync-issue "Fix message synchronization race condition"
/pr-chat bug/hotfix-auth "Fix authentication token refresh" main
```

## Workflow Steps

Execute the following steps in order:

### Step 1: Safety Validation

**CRITICAL - NEVER SKIP THIS STEP**

First, check the current branch:
```bash
git rev-parse --abbrev-ref HEAD
```

**STOP IMMEDIATELY** if the current branch is `main` or `develop`. Output an error message and do not proceed:
```
ERROR: Cannot commit directly to 'main' or 'develop'.
Please switch to a feature branch first or provide a branch name.
```

Also validate that the requested branch name is NOT `main` or `develop`.

### Step 2: Create and Checkout Branch

If a branch name is provided and you're not already on it:
```bash
git checkout -b <branch-name>
```

If the branch already exists:
```bash
git checkout <branch-name>
```

### Step 3: Stage Changes

Check for changes first:
```bash
git status
```

Stage the relevant changes (prefer staging specific files over `git add .` when possible).

### Step 4: Create Commit

Create a commit with the provided message. **Keep commit messages concise (1 line)** - detailed explanations belong in the PR description, not the commit message.

```bash
git commit -m "<commit-message>

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### Step 5: Push to Remote

Push the branch to origin:
```bash
git push -u origin <branch-name>
```

### Step 6: Create Draft PR

Create a **DRAFT** pull request targeting the `develop` branch.

**MANDATORY FLAGS:**
- `--draft` - ALWAYS create as draft, never a regular PR
- `--base develop` - ALWAYS target the develop branch
- `--label` - Add the appropriate label (see below)
- **NEVER** use `--reviewer` or any review request flags

**Label Selection:**

Auto-detect the label from the branch name prefix:

| Branch Prefix | Label |
|---------------|-------|
| `bug/` | `pr:bug` |
| `feature/` | `pr:new-feature` |

If the branch prefix doesn't match, ask the user to select from:
- `pr:bug` - Bug fix
- `pr:new-feature` - New feature
- `pr:improvement` - Improvement
- `pr:breaking-change` - Breaking change
- `pr:documentation` - Documentation
- `pr:dependencies` - Dependency updates
- `pr:internal` - Internal changes / housekeeping
- `pr:ci` - CI changes
- `pr:test` - Test-only changes
- `pr:demo-app` - Changes specific to demo app
- `pr:ignore-for-release` - Exclude from release notes

Use the project's PR template format:

```bash
gh pr create --draft --base develop --label "<label>" --title "<PR title>" --body "$(cat <<'EOF'
### Goal

<Explain why we are making this change>

### Implementation

<Describe the implementation>

### 🎨 UI Changes

<If no UI changes, write "No UI changes." Otherwise include before/after screenshots:>

| Before | After |
| --- | --- |
| img | img |

### Testing

<Explain how this change can be tested>

### ☑️Contributor Checklist

#### General
- [x] I have signed the [Stream CLA](https://docs.google.com/forms/d/e/1FAIpQLScFKsKkAJI7mhCr7K9rEIOpqIDThrWxuvxnwUq2XkHyG154vQ/viewform) (required)
- [ ] Assigned a person / code owner group (required)
- [ ] Thread with the PR link started in a respective Slack channel (required internally)
- [x] PR targets the `develop` branch
- [ ] PR is linked to the GitHub issue it resolves

#### Code & documentation
- [ ] New code is covered by unit tests
- [ ] Comparison screenshots added for visual changes
- [ ] Affected documentation updated (KDocs, docusaurus, tutorial)
EOF
)"
```

## Safety Rules (MANDATORY)

These rules MUST be followed and CANNOT be bypassed:

1. **NEVER commit to main or develop** - Always create/use a feature branch
2. **ALWAYS create DRAFT PRs** - Use `--draft` flag, no exceptions
3. **NEVER request reviews** - Do not use `--reviewer` or `--assignee` flags
4. **ALWAYS target develop** - PRs must target the `develop` branch

## Error Handling

- If on `main` or `develop`: Stop and ask user to provide a branch name
- If no changes to commit: Inform user and stop
- If push fails: Show error and suggest checking remote configuration
- If PR creation fails: Provide manual PR creation URL
