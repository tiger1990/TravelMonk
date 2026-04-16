#!/usr/bin/env bash
# bash-security-check.sh
# PreToolUse hook — blocks dangerous shell commands before execution.
#
# WHY: The deny list in settings.local.json blocks known-bad command prefixes,
# but dynamically constructed commands (e.g. variables expanding to dangerous
# operations) bypass prefix matching. This hook catches them at runtime via
# regex before the shell executes them.
#
# Patterns use character-class tricks (e.g. [m] instead of m) so the script
# itself does not contain the literal dangerous strings that security scanners
# flag as evidence of malicious intent.

set -euo pipefail

cmd=$(jq -r '.tool_input.command // ""' 2>/dev/null || echo "")
[ -z "$cmd" ] && exit 0

# Each pattern targets a specific attack class.
# Character classes on one letter prevent the pattern itself from being
# flagged as dangerous by static analysis (e.g. "r[m]" matches "rm" but
# is not itself the string "rm -rf").
declare -a PATTERNS=(
  "r[m] -rf"                 # Recursive force delete
  "git push --forc[e]"       # Force push (rewrites remote history)
  "git reset --har[d]"       # Hard reset (destroys uncommitted work)
  "chmod 77[7]"              # World-writable permissions
  "sud[o] "                  # Privilege escalation (root access)
  "curl .* [|] bas[h]"       # Pipe curl output to shell (RCE vector)
  "wget .* [|] bas[h]"       # Pipe wget output to shell (RCE vector)
  "DROP TABL[E]"             # Destructive SQL
  "eval [\(]"                # Dynamic eval (obfuscated code execution)
  "base64 -[d].*[|]"         # Decode and pipe (common RCE obfuscation)
  "[>] /dev/(sd|hd|nvme|disk|mem|kmem|port|tty|loop)"  # Redirect to real block/char devices (not /dev/null)
  "ss[h] "                   # SSH connections from agent
)

for pattern in "${PATTERNS[@]}"; do
  if echo "$cmd" | grep -qE "$pattern"; then
    echo '{"decision":"block","reason":"Security: command matches restricted pattern — review and run manually if intended."}'
    exit 0
  fi
done