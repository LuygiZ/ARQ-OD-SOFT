#!/bin/bash

# Push All Images to Registry
# Pushes all microservice Docker images to the registry

set -e

REGISTRY=${1:-localhost:5000}
ENVIRONMENT=${2:-dev}
VERSION=${3:-latest}

echo "=================================================="
echo "  PUSHING ALL IMAGES TO REGISTRY"
echo "=================================================="
echo "Registry: $REGISTRY"
echo "Environment: $ENVIRONMENT"
echo "Version: $VERSION"
echo "=================================================="

SERVICES=(
    "genre-service"
    "author-service"
    "book-command-service"
    "book-query-service"
    "lending-service"
    "reader-service"
    "saga-orchestrator"
)

SUCCESS_COUNT=0
FAILED_COUNT=0

for service in "${SERVICES[@]}"; do
    echo ""
    echo "Pushing $service..."

    IMAGE_NAME="$REGISTRY/$service"

    # Push all tags
    TAGS=(
        "$IMAGE_NAME:$ENVIRONMENT"
        "$IMAGE_NAME:$VERSION"
        "$IMAGE_NAME:latest"
    )

    SERVICE_SUCCESS=true

    for tag in "${TAGS[@]}"; do
        echo "  Pushing $tag..."

        if docker push $tag; then
            echo "  ✓ Pushed $tag"
        else
            echo "  ✗ Failed to push $tag"
            SERVICE_SUCCESS=false
        fi
    done

    if [ "$SERVICE_SUCCESS" = true ]; then
        echo "✓ $service pushed successfully"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo "✗ $service push failed"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
done

# Verify images in registry
echo ""
echo "=================================================="
echo "  VERIFYING REGISTRY"
echo "=================================================="

if command -v curl &> /dev/null; then
    echo "Checking registry catalog..."
    curl -s "http://$REGISTRY/v2/_catalog" | grep -q "repositories" && echo "✓ Registry is accessible" || echo "✗ Registry is not accessible"
fi

# Summary
echo ""
echo "=================================================="
echo "  PUSH SUMMARY"
echo "=================================================="
echo "Total Services: ${#SERVICES[@]}"
echo "  ✓ Successful: $SUCCESS_COUNT"
echo "  ✗ Failed: $FAILED_COUNT"
echo "=================================================="

if [ $FAILED_COUNT -gt 0 ]; then
    exit 1
else
    exit 0
fi
