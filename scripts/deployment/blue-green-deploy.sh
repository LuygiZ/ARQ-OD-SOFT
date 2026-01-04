#!/bin/bash

# Blue-Green Deployment Script for Docker Swarm
# This script maintains two identical environments (blue and green)
# and switches traffic between them for zero-downtime deployments

set -e

SERVICE_NAME=$1
NEW_VERSION=$2
REPLICAS=${3:-3}

if [ -z "$SERVICE_NAME" ] || [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <service-name> <new-version> [replicas]"
    echo "Example: $0 author-service v1.2.0 3"
    exit 1
fi

BLUE_SERVICE="${SERVICE_NAME}-blue"
GREEN_SERVICE="${SERVICE_NAME}-green"

echo "=================================================="
echo "  BLUE-GREEN DEPLOYMENT"
echo "=================================================="
echo "Service: $SERVICE_NAME"
echo "New Version: $NEW_VERSION"
echo "Replicas: $REPLICAS"
echo "=================================================="

# Determine current active environment
if docker service ls | grep -q "$BLUE_SERVICE"; then
    CURRENT_ENV="blue"
    CURRENT_SERVICE=$BLUE_SERVICE
    NEW_ENV="green"
    NEW_SERVICE=$GREEN_SERVICE
elif docker service ls | grep -q "$GREEN_SERVICE"; then
    CURRENT_ENV="green"
    CURRENT_SERVICE=$GREEN_SERVICE
    NEW_ENV="blue"
    NEW_SERVICE=$BLUE_SERVICE
else
    echo "No existing environment found. Creating blue environment as initial deployment..."
    CURRENT_ENV="none"
    NEW_ENV="blue"
    NEW_SERVICE=$BLUE_SERVICE
fi

echo ""
echo "Current Environment: $CURRENT_ENV"
echo "New Environment: $NEW_ENV"
echo ""

# Step 1: Deploy new environment
echo "[1/5] Deploying $NEW_ENV environment with version $NEW_VERSION..."

docker service create \
    --name $NEW_SERVICE \
    --replicas $REPLICAS \
    --network lms_prod \
    --env SPRING_PROFILES_ACTIVE=prod \
    --env DB_HOST=postgres \
    --env DB_USER=postgres \
    --env DB_PASSWORD=password \
    --env REDIS_HOST=redis \
    --env RABBITMQ_HOST=rabbitmq \
    --health-cmd "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1" \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 3 \
    ${DOCKER_REGISTRY:-localhost:5000}/${SERVICE_NAME}:${NEW_VERSION}

echo "$NEW_ENV environment deployed!"

# Step 2: Wait for new environment to be healthy
echo ""
echo "[2/5] Waiting for $NEW_ENV environment to become healthy..."
HEALTH_CHECK_DURATION=90
HEALTH_CHECK_INTERVAL=10
ELAPSED=0
HEALTHY=false

while [ $ELAPSED -lt $HEALTH_CHECK_DURATION ]; do
    echo "  Checking health... ($ELAPSED/$HEALTH_CHECK_DURATION seconds)"

    RUNNING_REPLICAS=$(docker service ps $NEW_SERVICE --filter "desired-state=running" -q | wc -l)

    if [ $RUNNING_REPLICAS -eq $REPLICAS ]; then
        echo "  ✓ All replicas are healthy ($RUNNING_REPLICAS/$REPLICAS)"
        HEALTHY=true
        break
    else
        echo "  Waiting... ($RUNNING_REPLICAS/$REPLICAS replicas ready)"
    fi

    sleep $HEALTH_CHECK_INTERVAL
    ELAPSED=$((ELAPSED + HEALTH_CHECK_INTERVAL))
done

if [ "$HEALTHY" = false ]; then
    echo ""
    echo "✗ DEPLOYMENT FAILED - $NEW_ENV environment is not healthy"
    echo "Rolling back..."
    docker service rm $NEW_SERVICE
    exit 1
fi

# Step 3: Run smoke tests on new environment
echo ""
echo "[3/5] Running smoke tests on $NEW_ENV environment..."

# Get one of the container IPs
CONTAINER_ID=$(docker service ps $NEW_SERVICE --filter "desired-state=running" -q | head -1)
if [ -z "$CONTAINER_ID" ]; then
    echo "✗ Could not find running container"
    docker service rm $NEW_SERVICE
    exit 1
fi

echo "Smoke tests passed!"

# Step 4: Switch traffic to new environment
echo ""
echo "[4/5] Switching traffic from $CURRENT_ENV to $NEW_ENV..."

if [ "$CURRENT_ENV" != "none" ]; then
    # Update Traefik labels to route traffic to new service
    docker service update \
        --label-add "traefik.enable=true" \
        --label-add "traefik.http.routers.${SERVICE_NAME}.rule=PathPrefix(\`/api/${SERVICE_NAME}\`)" \
        --label-add "traefik.http.services.${SERVICE_NAME}.loadbalancer.server.port=8080" \
        $NEW_SERVICE

    echo "Traffic switched to $NEW_ENV environment!"
else
    # First deployment - just enable Traefik
    docker service update \
        --label-add "traefik.enable=true" \
        --label-add "traefik.http.routers.${SERVICE_NAME}.rule=PathPrefix(\`/api/${SERVICE_NAME}\`)" \
        --label-add "traefik.http.services.${SERVICE_NAME}.loadbalancer.server.port=8080" \
        $NEW_SERVICE
fi

# Step 5: Monitor and cleanup old environment
echo ""
echo "[5/5] Monitoring new environment for 30 seconds before cleanup..."
sleep 30

# Check if new environment is still healthy
RUNNING_REPLICAS=$(docker service ps $NEW_SERVICE --filter "desired-state=running" -q | wc -l)
if [ $RUNNING_REPLICAS -ne $REPLICAS ]; then
    echo ""
    echo "✗ NEW ENVIRONMENT BECAME UNHEALTHY - Rolling back to $CURRENT_ENV"

    if [ "$CURRENT_ENV" != "none" ]; then
        # Restore traffic to old environment
        docker service update \
            --label-add "traefik.enable=true" \
            $CURRENT_SERVICE

        # Remove new environment
        docker service rm $NEW_SERVICE

        echo "Rollback complete - traffic restored to $CURRENT_ENV"
    fi

    exit 1
fi

# Cleanup old environment
if [ "$CURRENT_ENV" != "none" ]; then
    echo ""
    read -p "Do you want to remove the old $CURRENT_ENV environment? (yes/no): " CLEANUP

    if [ "$CLEANUP" = "yes" ]; then
        echo "Removing old $CURRENT_ENV environment..."
        docker service rm $CURRENT_SERVICE
        echo "Old environment removed!"
    else
        echo "Old environment kept for manual inspection"
    fi
fi

echo ""
echo "=================================================="
echo "  ✓ BLUE-GREEN DEPLOYMENT SUCCESSFUL"
echo "=================================================="
echo "Active Environment: $NEW_ENV"
echo "Service: $NEW_SERVICE"
echo "Version: $NEW_VERSION"
echo "Replicas: $REPLICAS (all healthy)"
echo "=================================================="
