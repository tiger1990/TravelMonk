#!/usr/bin/env bash
# session-end-check.sh
# Stop hook — runs when Claude Code session ends.
#
# WHY: Session-end is the last chance to catch issues before the user
# closes the workspace. This hook checks for two common post-session risks:
#   1. Sensitive files appearing in git diff (accidental credential staging)
#   2. HttpLoggingInterceptor left enabled (documented gap in architecture_gaps.md)
#
# Outputs a systemMessage warning visible in the Claude Code UI.
# Does NOT block — only informs. Exit 0 always.

set -uo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel 2>/dev/null || echo "")"
[ -z "$REPO_ROOT" ] && exit 0

warnings=()

# Check 1: sensitive files modified in working tree or index
if git -C "$REPO_ROOT" diff --name-only HEAD 2>/dev/null \
   | grep -qE '(\.env|local\.properties|\.jks|\.keystore|signing\.gradle)'; then
  warnings+=("Sensitive file modified — verify no secrets are staged or committed.")
fi

# Check 2: HttpLoggingInterceptor enabled outside DEBUG guard
# (architecture_gaps.md: never leave logging enabled in release builds)
if grep -r "HttpLoggingInterceptor" \
   "$REPO_ROOT/core/network/src/main" 2>/dev/null \
   | grep -v "BuildConfig.DEBUG" | grep -q "BODY\|HEADERS"; then
  warnings+=("HttpLoggingInterceptor may be enabled outside DEBUG — check ArchitectureGaps.md.")
fi

if [ ${#warnings[@]} -gt 0 ]; then
  joined=$(printf ' | %s' "${warnings[@]}")
  joined="${joined:3}"
  printf '{"systemMessage":"Session-end check: %s"}' "$joined"
fi

exit 0