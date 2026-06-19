#!/usr/bin/env bash
#
# setup-agent-skills.sh — restore the .agent/skills symlinks on any clone.
#
# The symlinks under .agent/skills point at the "compose-performance-skills"
# source, which lives OUTSIDE the repo (it is not vendored here). Because the
# target is an absolute, machine-specific path, the committed symlinks break on
# every fresh clone. Run this script once after cloning to recreate them for the
# current machine.
#
# Source resolution order:
#   1. $COMPOSE_SKILLS_SRC               (explicit override, if it exists)
#   2. ~/.claude/skills-sources/compose-performance-skills   (default location)
#   3. git clone from $SKILLS_REPO into the default location  (auto-fetch)
#
# Usage:
#   ./scripts/setup-agent-skills.sh
#   COMPOSE_SKILLS_SRC=/path/to/source ./scripts/setup-agent-skills.sh
#
set -euo pipefail

SKILLS_REPO="https://github.com/skydoves/compose-performance-skills.git"
DEFAULT_SRC="${HOME}/.claude/skills-sources/compose-performance-skills"

# Repo root = parent of this script's directory, resolved absolutely.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SKILLS_DIR="${REPO_ROOT}/.agent/skills"

# Category/skill subpaths, relative to the source root. The symlink name is the
# basename of each entry. Keep this list in sync with .agent/skills.
SUBPATHS="
audit/auditing-compose-performance
build/configuring-r8-for-compose
hot-reload/iterating-with-ai-and-mcp
hot-reload/preserving-state-across-reloads
hot-reload/setting-up-compose-hotswan
hot-reload/understanding-hot-reload-limits
lists/configuring-lazy-prefetch
lists/optimizing-lazy-layouts
measurement/generating-baseline-profiles
measurement/testing-compose-in-release-mode
measurement/tracing-recompositions-at-runtime
modifiers/migrating-to-modifier-node
modifiers/ordering-modifier-chains
recomposition/avoiding-subcomposition-pitfalls
recomposition/choosing-derivedstateof
recomposition/debugging-recompositions
recomposition/deferring-state-reads
recomposition/using-strong-skipping-correctly
side-effects/collecting-flows-safely
side-effects/using-efficient-effects
stability/diagnosing-compose-stability
stability/enforcing-stability-in-ci
stability/stabilizing-compose-types
stability/understanding-stability-inference
stability/using-stability-analyzer-ide-plugin
stability/visualizing-recomposition-cascades
"

log()  { printf '%s\n' "$*"; }
die()  { printf 'error: %s\n' "$*" >&2; exit 1; }

resolve_source() {
  # 1. Explicit override.
  if [ -n "${COMPOSE_SKILLS_SRC:-}" ]; then
    [ -d "${COMPOSE_SKILLS_SRC}" ] \
      || die "COMPOSE_SKILLS_SRC is set but does not exist: ${COMPOSE_SKILLS_SRC}"
    printf '%s' "${COMPOSE_SKILLS_SRC}"
    return
  fi

  # 2. Default location already present.
  if [ -d "${DEFAULT_SRC}" ]; then
    printf '%s' "${DEFAULT_SRC}"
    return
  fi

  # 3. Auto-clone into the default location.
  command -v git >/dev/null 2>&1 || die "git not found; cannot fetch skills source"
  log "Source not found — cloning ${SKILLS_REPO}" >&2
  log "  into ${DEFAULT_SRC}" >&2
  mkdir -p "$(dirname "${DEFAULT_SRC}")"
  git clone --depth 1 "${SKILLS_REPO}" "${DEFAULT_SRC}" >&2
  printf '%s' "${DEFAULT_SRC}"
}

main() {
  local src
  src="$(resolve_source)"
  log "Using source: ${src}"

  mkdir -p "${SKILLS_DIR}"

  local created=0 missing=0
  for sub in ${SUBPATHS}; do
    local name target link
    name="$(basename "${sub}")"
    target="${src}/${sub}"
    link="${SKILLS_DIR}/${name}"

    if [ ! -d "${target}" ]; then
      printf '  MISSING in source: %s\n' "${sub}" >&2
      missing=$((missing + 1))
      continue
    fi

    ln -snf "${target}" "${link}"
    created=$((created + 1))
  done

  log "Linked ${created} skill(s) into .agent/skills"
  [ "${missing}" -eq 0 ] || die "${missing} skill(s) not found in source — is it the right repo/version?"

  # Final integrity check: no dangling links.
  local broken=0 l
  for l in "${SKILLS_DIR}"/*; do
    [ -e "${l}" ] || { printf '  BROKEN: %s\n' "${l##*/}" >&2; broken=$((broken + 1)); }
  done
  [ "${broken}" -eq 0 ] || die "${broken} symlink(s) still dangling"
  log "All .agent/skills symlinks resolve. Done."
}

main "$@"
