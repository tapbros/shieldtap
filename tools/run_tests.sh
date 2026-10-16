#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$(mktemp -d "${TMPDIR:-/tmp}/shieldtap-jvm.XXXXXX")"
trap 'find "$OUT" -type f -delete; find "$OUT" -depth -type d -exec rmdir {} \;' EXIT

# Only JVM boundary fixtures plus the actual production service/color implementation.
find "$ROOT/tests/jvm" -name '*.java' -print > "$OUT/sources.list"
printf '%s\n' "$ROOT/src/com/gml/autoblocker/AutoTapService.java" \
    "$ROOT/src/com/gml/autoblocker/HexColor.java" >> "$OUT/sources.list"
mkdir "$OUT/classes"
javac -encoding UTF-8 --release 8 -d "$OUT/classes" @"$OUT/sources.list"
java -cp "$OUT/classes" com.gml.autoblocker.AutoTapServiceTest
