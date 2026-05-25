#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

branch="$(git branch --show-current)"
if [[ -z "$branch" ]]; then
  echo "Error: not on a Git branch."
  exit 1
fi

remote="${GIT_REMOTE:-origin}"
message="${1:-Update project}"

if ! git remote get-url "$remote" >/dev/null 2>&1; then
  echo "Error: Git remote '$remote' is not configured."
  exit 1
fi

git add -A

if ! git diff --cached --quiet; then
  git commit -m "$message"
else
  echo "No local changes to commit."
fi

git fetch "$remote" "$branch"

if git rev-parse --verify --quiet "refs/remotes/$remote/$branch" >/dev/null; then
  git rebase "$remote/$branch"
else
  echo "Remote branch '$remote/$branch' does not exist yet; pushing a new branch."
fi

git push -u "$remote" "$branch"
