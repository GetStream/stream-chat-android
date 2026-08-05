#!/usr/bin/env bash
#
# Download Paparazzi goldens recorded by the "Record Paparazzi Snapshots" CI workflow and replace
# the local golden images with them, so the committed references match CI's (Linux/amd64) renderer.
#
# This is the authoritative way to (re)generate goldens — the CI runner is the environment that
# verifies them. Do NOT record on macOS; those pixels won't match CI.
#
# Prerequisites: gh CLI authenticated (`gh auth login`), and a completed run of the Record workflow.
#
# Usage:
#   scripts/paparazzi-pull-goldens.sh            # use the latest Record workflow run
#   scripts/paparazzi-pull-goldens.sh <run-id>   # use a specific run
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

WORKFLOW="Record Paparazzi Snapshots"
ARTIFACT="paparazzi-goldens"

run_id="${1:-}"
if [[ -z "$run_id" ]]; then
  echo "==> finding latest '$WORKFLOW' run"
  run_id="$(gh run list --workflow "$WORKFLOW" -L 1 --json databaseId -q '.[0].databaseId')"
fi
[[ -n "$run_id" ]] || { echo "error: no run id (run the workflow first)" >&2; exit 1; }

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

echo "==> downloading '$ARTIFACT' from run $run_id"
gh run download "$run_id" -n "$ARTIFACT" -D "$tmp"

echo "==> replacing local Paparazzi snapshots"
# Remove existing goldens first so deletions/renames made by cleanRecord are reflected locally.
find . -type d -path '*/src/test/snapshots' -not -path '*/build/*' -exec rm -rf {} +
# Overlay the downloaded set (artifact preserves <module>/src/test/snapshots/... paths).
cp -R "$tmp"/. "$REPO_ROOT"/

# Stage only snapshot paths (adds, changes, and deletions), leaving everything else untouched.
git add -A -- ':(glob)**/src/test/snapshots/**'

echo
echo "==> staged snapshot changes:"
git status --short -- ':(glob)**/src/test/snapshots/**' | head -30
echo
echo "Review the diff, then commit and push:"
echo "  git commit -m 'test: re-record paparazzi goldens on CI (linux/amd64)'"
echo "  git push"
