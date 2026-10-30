#!/usr/bin/env bash
# Fails the release when the Play notes in whatsnew/ are missing, unchanged since the previous
# stable tag (old notes would ship again), empty, or longer than Play's 500 characters per locale.
set -euo pipefail
export LC_ALL=C.UTF-8
dir="${1:-whatsnew}"

if [ ! -s "$dir/whatsnew-en-US" ]; then
  echo "::error::$dir/whatsnew-en-US is missing or empty"
  exit 1
fi

prev_tag=$(git describe --tags --abbrev=0 --match 'v[0-9]*' --exclude '*-*' HEAD^ 2>/dev/null || true)
if [ -n "$prev_tag" ] && git diff --quiet "$prev_tag" HEAD -- "$dir/"; then
  echo "::error::$dir/ is unchanged since $prev_tag; write the notes for this release"
  exit 1
fi
echo "$dir/ changed since ${prev_tag:-<no previous tag>}"

for f in "$dir"/whatsnew-*; do
  n=$(wc -m < "$f")
  echo "--- $f ($n chars) ---"; cat "$f"; echo
  if [ "$n" -eq 0 ]; then echo "::error::$f is empty"; exit 1; fi
  if [ "$n" -gt 500 ]; then echo "::error::$f exceeds 500 chars ($n)"; exit 1; fi
done
