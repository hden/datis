#!/bin/sh
set -eu

hooks_dir=$(git rev-parse --git-path hooks)
mkdir -p "$hooks_dir"
install -m 0755 scripts/git-hooks/pre-commit "$hooks_dir/pre-commit"
install -m 0755 scripts/git-hooks/pre-push "$hooks_dir/pre-push"
