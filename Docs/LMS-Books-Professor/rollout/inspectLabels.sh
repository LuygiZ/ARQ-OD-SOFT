#!/usr/bin/env bash
set -euo pipefail

BLUE_SERVICE="lmsbooks_blue"
GREEN_SERVICE="lmsbooks_green"

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <blue|green>"
  exit 1
fi

TARGET="$1"

case "$TARGET" in
  blue)
    SERVICE="$BLUE_SERVICE"
    ;;
  green)
    SERVICE="$GREEN_SERVICE"
    ;;
  *)
    echo "Invalid target: $TARGET"
    exit 1
    ;;
esac

# First ensure the service exists
if ! docker service ls --format '{{.Name}}' | grep -q "^${SERVICE}$"; then
  echo "Error: service '${SERVICE}' not found"
  exit 1
fi

echo "Inspecting labels for service: ${SERVICE}"
echo "------------------------------------------"

docker service inspect "${SERVICE}" --format '{{json .Spec.Labels}}' | jq