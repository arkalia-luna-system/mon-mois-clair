#!/usr/bin/env bash
# Push / commit sur volume T7 : empêche AppleDouble (._*) de polluer .git
# (sinon « non-monotonic index » sur ._pack-*.idx) et le working tree.
set -euo pipefail
export COPYFILE_DISABLE=1
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
# Sidecars dans .git — jamais les vrais objets.
if [[ -d .git ]]; then
  find .git -name '._*' -type f -delete 2>/dev/null || true
fi
# Working tree (sources / docs) — hors caches Gradle / build (régénérés).
find . -name '._*' -type f \
  -not -path './.git/*' \
  -not -path './.gradle/*' \
  -not -path '*/build/*' \
  -delete 2>/dev/null || true
exec git "$@"
