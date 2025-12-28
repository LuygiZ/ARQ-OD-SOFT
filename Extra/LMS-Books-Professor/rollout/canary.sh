#!/usr/bin/env bash
set -euo pipefail

BLUE_SERVICE="lmsbooks_blue"
GREEN_SERVICE="lmsbooks_green"

PARENT="lmsbooks_service"

# Child backends
BLUE_BACKEND="blue_backend"
GREEN_BACKEND="green_backend"

# Helper: read traefik.enable
get_enable() {
  docker service inspect "$1" --format '{{ index .Spec.Labels "traefik.enable" }}'
}

BLUE_ENABLE=$(get_enable "$BLUE_SERVICE")
GREEN_ENABLE=$(get_enable "$GREEN_SERVICE")

BLUE_ENABLE=${BLUE_ENABLE:-false}
GREEN_ENABLE=${GREEN_ENABLE:-false}

echo "blue enabled?  $BLUE_ENABLE"
echo "green enabled? $GREEN_ENABLE"
echo ""

# Decide weights
if [[ "$BLUE_ENABLE" == "true" && "$GREEN_ENABLE" == "false" ]]; then
    echo "Blue only → blue=100, green=0"
    BLUE_WEIGHT=100
    GREEN_WEIGHT=0

elif [[ "$BLUE_ENABLE" == "false" && "$GREEN_ENABLE" == "true" ]]; then
    echo "Green only → blue=0, green=100"
    BLUE_WEIGHT=0
    GREEN_WEIGHT=100

elif [[ "$BLUE_ENABLE" == "true" && "$GREEN_ENABLE" == "true" ]]; then
    echo "Both enabled → blue=50, green=50"
    BLUE_WEIGHT=50
    GREEN_WEIGHT=50

else
    echo "Both disabled → fallback blue=100, green=0"
    BLUE_WEIGHT=100
    GREEN_WEIGHT=0
fi

# Update weights
echo ""
echo "Applying weights..."
echo "blue_backend  → ${BLUE_WEIGHT}"
echo "green_backend → ${GREEN_WEIGHT}"

docker service update "$BLUE_SERVICE" \
  --label-rm "traefik.http.services.${PARENT}.loadbalancer.server.port=8080" \
  --label-add "traefik.http.services.${BLUE_BACKEND}.loadbalancer.server.port=8080" \
  --label-add "traefik.http.services.${PARENT}.loadbalancer.services.${BLUE_BACKEND}.weight=${BLUE_WEIGHT}" \

docker service update "$GREEN_SERVICE" \
  --label-rm "traefik.http.services.${PARENT}.loadbalancer.server.port=8080" \
  --label-add "traefik.http.services.${GREEN_BACKEND}.loadbalancer.server.port=8080" \
  --label-add "traefik.http.services.${PARENT}.loadbalancer.services.${GREEN_BACKEND}.weight=${GREEN_WEIGHT}" \

${inspect-}

echo ""
echo "✔ Traefik weights updated."