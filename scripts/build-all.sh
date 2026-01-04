#!/bin/bash

# Build All Services Script
# Builds all microservices and creates Docker images

set -e

echo "=================================================="
echo "  BUILDING ALL LMS MICROSERVICES"
echo "=================================================="
echo "Timestamp: $(date)"
echo ""

# Configuration
SERVICES=(
    "genre-service"
    "author-service"
    "book-command-service"
    "book-query-service"
    "lending-service"
    "reader-service"
    "saga-orchestrator"
)

ENVIRONMENT=${1:-dev}
REGISTRY=${2:-localhost:5000}
VERSION=${3:-latest}

echo "Environment: $ENVIRONMENT"
echo "Registry: $REGISTRY"
echo "Version: $VERSION"
echo "=================================================="

# Step 1: Build shared kernel
echo ""
echo "[1/3] Building shared-kernel..."
cd shared-kernel
mvn clean install -DskipTests
cd ..
echo "✓ Shared kernel built successfully"

# Step 2: Build all services
echo ""
echo "[2/3] Building all services..."
mvn clean package -DskipTests

SUCCESS_COUNT=0
FAILED_COUNT=0
FAILED_SERVICES=()

for service in "${SERVICES[@]}"; do
    if [ -f "$service/target/*.jar" ]; then
        echo "✓ $service built successfully"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo "✗ $service build failed"
        FAILED_COUNT=$((FAILED_COUNT + 1))
        FAILED_SERVICES+=("$service")
    fi
done

# Step 3: Build Docker images
echo ""
echo "[3/3] Building Docker images..."

for service in "${SERVICES[@]}"; do
    # Skip if service build failed
    if [[ " ${FAILED_SERVICES[@]} " =~ " ${service} " ]]; then
        echo "⊘ Skipping $service (build failed)"
        continue
    fi

    echo "Building Docker image for $service..."

    IMAGE_NAME="$REGISTRY/$service"
    TAGS=(
        "$IMAGE_NAME:$ENVIRONMENT"
        "$IMAGE_NAME:$VERSION"
        "$IMAGE_NAME:latest"
    )

    cd $service

    # Build with multiple tags
    TAG_ARGS=""
    for tag in "${TAGS[@]}"; do
        TAG_ARGS="$TAG_ARGS -t $tag"
    done

    docker build $TAG_ARGS .

    if [ $? -eq 0 ]; then
        echo "✓ Docker image built: $service"
    else
        echo "✗ Docker image build failed: $service"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi

    cd ..
done

# Summary
echo ""
echo "=================================================="
echo "  BUILD SUMMARY"
echo "=================================================="
echo "Total Services: ${#SERVICES[@]}"
echo "  ✓ Successful: $SUCCESS_COUNT"
echo "  ✗ Failed: $FAILED_COUNT"

if [ $FAILED_COUNT -gt 0 ]; then
    echo ""
    echo "Failed services:"
    for service in "${FAILED_SERVICES[@]}"; do
        echo "  - $service"
    done
fi

echo "=================================================="
echo "Build completed at $(date)"
echo "=================================================="

if [ $FAILED_COUNT -gt 0 ]; then
    exit 1
else
    exit 0
fi
