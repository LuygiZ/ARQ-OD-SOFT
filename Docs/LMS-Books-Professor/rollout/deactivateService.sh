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

echo "Enabling Traefik for service: $SERVICE"

docker service update "$SERVICE" \
  --label-rm traefik.enable \
  --label-add traefik.enable=false \
  --label-rm traefik.http.routers.lmsbooks_router_ab.entrypoints \
  --label-rm traefik.http.routers.lmsbooks_router_ab.service \
  --label-rm traefik.http.services.lmsbooks_service_ab.loadbalancer.server.port \
  --label-rm traefik.http.routers.lmsbooks_router_ab.rule

echo "Traefik enabled for $SERVICE (all replicas active)"