#!/usr/bin/env bash
# Downloads an OpenStreetMap extract from Geofabrik and verifies its checksum.
# Usage: scripts/fetch-osm.sh [extract-path]
#   e.g. scripts/fetch-osm.sh europe/france/ile-de-france   (default)
set -euo pipefail

EXTRACT="${1:-europe/france/ile-de-france}"
BASE="https://download.geofabrik.de/${EXTRACT}-latest.osm.pbf"
OUT="$(dirname "$0")/../graphhopper"

curl -fL --proto '=https' --tlsv1.2 -o "${OUT}/region.osm.pbf" "${BASE}"
curl -fL --proto '=https' --tlsv1.2 -o "${OUT}/region.osm.pbf.md5" "${BASE}.md5"

expected="$(cut -d' ' -f1 "${OUT}/region.osm.pbf.md5")"
actual="$(md5sum "${OUT}/region.osm.pbf" | cut -d' ' -f1)"
if [ "${expected}" != "${actual}" ]; then
  echo "Checksum mismatch: refusing to use this file." >&2
  rm -f "${OUT}/region.osm.pbf"
  exit 1
fi
echo "OK: ${OUT}/region.osm.pbf"
