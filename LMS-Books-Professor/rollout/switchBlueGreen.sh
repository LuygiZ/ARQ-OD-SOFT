#!/usr/bin/env bash
set -euo pipefail

# Adjust these if your service names are different
BLUE_SERVICE="lmsbooks_blue"
GREEN_SERVICE="lmsbooks_green"

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 blue|green"
  exit 1
fi

TARGET="$1"

case "$TARGET" in
  blue)
    ACTIVE_SERVICE="$BLUE_SERVICE"
    INACTIVE_SERVICE="$GREEN_SERVICE"
    ;;
  green)
    ACTIVE_SERVICE="$GREEN_SERVICE"
    INACTIVE_SERVICE="$BLUE_SERVICE"
    ;;
  *)
    echo "Invalid target: $TARGET"
    echo "Usage: $0 blue|green"
    exit 1
    ;;
esac

echo "Enabling Traefik on:  $ACTIVE_SERVICE"
docker service update "$ACTIVE_SERVICE" \
  --label-rm traefik.enable \
  --label-add traefik.enable=true

echo "Disabling Traefik on: $INACTIVE_SERVICE"
docker service update "$INACTIVE_SERVICE" \
  --label-rm traefik.enable \
  --label-add traefik.enable=false

echo "Switched traffic to: $TARGET"