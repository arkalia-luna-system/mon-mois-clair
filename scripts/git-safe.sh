#!/usr/bin/env bash
# Push / commit sur volume T7 : empêche AppleDouble (._*) de polluer .git
# (sinon « non-monotonic index » sur ._pack-*.idx).
set -euo pipefail
export COPYFILE_DISABLE=1
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
# Sidecars dans .git uniquement — jamais les vrais objets.
if [[ -d .git ]]; then
  find .git -name '._*' -type f -delete 2>/dev/null || true
fi
exec git "$@"
