#!/bin/bash

# Validate input
if [ -z "$1" ]; then
  echo "Usage: $0 <blue|green>"
  exit 1
fi

# Map shorthand to Docker service name
if [ "$1" == "blue" ]; then
  SERVICE="lmsbooks_blue"
elif [ "$1" == "green" ]; then
  SERVICE="lmsbooks_green"
else
  echo "Invalid value: $1"
  echo "Expected: blue or green"
  exit 1
fi

echo "Updating Traefik labels for service: $SERVICE"

# Backend name (matches the docker service name)
BACKEND="$SERVICE"

# Apply label reset + new labels
docker service update \
  --label-rm traefik.enable \
  --label-rm traefik.http.routers.lmsbooks_router_canary_with_config_file.entrypoints \
  --label-rm traefik.http.routers.lmsbooks_router_canary_with_config_file.service \
  --label-rm traefik.http.services.${BACKEND}.loadbalancer.server.port \
\
  --label-add traefik.enable=true \
  --label-add traefik.http.routers.lmsbooks_router_canary_with_config_file.entrypoints=web \
  --label-add traefik.http.routers.lmsbooks_router_canary_with_config_file.service=lmsbooks_split@file \
  --label-add traefik.http.services.${BACKEND}.loadbalancer.server.port=8080 \
  "$SERVICE"

echo "Labels updated successfully for $SERVICE."