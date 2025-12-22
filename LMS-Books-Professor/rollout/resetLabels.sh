#!/bin/bash

SERVICE="$1"

#if [ -z "$SERVICE" ]; then
#  echo "Usage: $0 <service_name>"
#  exit 1
#fi

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

echo "Resetting all Traefik labels on service: $SERVICE"

# Get all existing Traefik labels
LABELS=$(docker service inspect "$SERVICE" --format '{{json .Spec.Labels}}' | jq -r 'keys[]' | grep '^traefik\.')

if [ -z "$LABELS" ]; then
  echo "No Traefik labels found on service $SERVICE."
  exit 0
fi

# Build the --label-rm arguments dynamically
REMOVE_ARGS=()
for LABEL in $LABELS; do
  REMOVE_ARGS+=(--label-rm "$LABEL")
done

echo "Removing labels:"
printf ' - %s\n' $LABELS

# Apply removal
docker service update "${REMOVE_ARGS[@]}" "$SERVICE"