#!/usr/bin/env bash
# file-sensitivity-check.sh
# PreToolUse hook — blocks Write/Edit to credential and signing files.
#
# WHY: Android projects store signing keys, API keys, and local configuration
# in files that must never be touched by automated tooling. A compromised
# or mistaken write to these files could leak credentials or break release
# signing. This hook intercepts Write and Edit tool calls before they execute.
#
# Matched files:
#   .env / .env.*          — environment variable files (API keys, secrets)
#   local.properties       — Android SDK path + often stores key passwords
#   *keystore*             — Android keystore directory or filename fragments
#   signing.gradle         — Gradle signing config (contains keystore paths/passwords)
#   *.jks / *.keystore     — Binary keystore files (private signing keys)

set -euo pipefail

path=$(jq -r '.tool_input.file_path // ""' 2>/dev/null || echo "")
[ -z "$path" ] && exit 0

if echo "$path" | grep -qE '(\.env$|\.env\..*|local\.properties$|keystore|signing\.gradle|\.jks$|\.keystore$)'; then
  echo '{"decision":"block","reason":"Security: file contains credentials or signing config — automated edits are blocked. Edit manually if intended."}'
fi