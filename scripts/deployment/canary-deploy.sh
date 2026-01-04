#!/bin/bash

# Canary Deployment Script for Docker Swarm
# This script deploys a new version alongside the old one with reduced traffic (canary pattern)

set -e

SERVICE_NAME=$1
NEW_VERSION=$2
CANARY_REPLICAS=${3:-1}
TOTAL_REPLICAS=${4:-3}

if [ -z "$SERVICE_NAME" ] || [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <service-name> <new-version> [canary-replicas] [total-replicas]"
    echo "Example: $0 genre-service v1.2.0 1 3"
    exit 1
fi

CANARY_SERVICE="${SERVICE_NAME}-canary"
STABLE_REPLICAS=$((TOTAL_REPLICAS - CANARY_REPLICAS))

echo "=================================================="
echo "  CANARY DEPLOYMENT"
echo "=================================================="
echo "Service: $SERVICE_NAME"
echo "New Version: $NEW_VERSION"
echo "Canary Replicas: $CANARY_REPLICAS"
echo "Stable Replicas: $STABLE_REPLICAS"
echo "Total Replicas: $TOTAL_REPLICAS"
echo "=================================================="

# Step 1: Scale down stable version
echo ""
echo "[1/5] Scaling down stable version to $STABLE_REPLICAS replicas..."
docker service scale ${SERVICE_NAME}=${STABLE_REPLICAS}

sleep 5

# Step 2: Deploy canary version
echo ""
echo "[2/5] Deploying canary version..."

# Get the current service configuration
docker service inspect ${SERVICE_NAME} > /tmp/${SERVICE_NAME}-config.json

# Create canary service with new version
docker service create \
    --name ${CANARY_SERVICE} \
    --replicas ${CANARY_REPLICAS} \
    --network lms_prod \
    --env SPRING_PROFILES_ACTIVE=prod \
    --label traefik.enable=true \
    --label "traefik.http.services.${CANARY_SERVICE}.loadbalancer.server.port=8080" \
    ${DOCKER_REGISTRY:-localhost:5000}/${SERVICE_NAME}:${NEW_VERSION}

echo "Canary service created: ${CANARY_SERVICE}"

# Step 3: Monitor canary health
echo ""
echo "[3/5] Monitoring canary health for 60 seconds..."
HEALTH_CHECK_DURATION=60
HEALTH_CHECK_INTERVAL=10
ELAPSED=0

while [ $ELAPSED -lt $HEALTH_CHECK_DURATION ]; do
    echo "  Checking canary health... ($ELAPSED/$HEALTH_CHECK_DURATION seconds)"

    # Check if canary replicas are running
    RUNNING_REPLICAS=$(docker service ps ${CANARY_SERVICE} --filter "desired-state=running" -q | wc -l)

    if [ $RUNNING_REPLICAS -eq $CANARY_REPLICAS ]; then
        echo "  ✓ Canary is healthy ($RUNNING_REPLICAS/$CANARY_REPLICAS replicas running)"
    else
        echo "  ✗ Canary is unhealthy ($RUNNING_REPLICAS/$CANARY_REPLICAS replicas running)"
        echo ""
        echo "CANARY DEPLOYMENT FAILED - Rolling back..."
        docker service rm ${CANARY_SERVICE}
        docker service scale ${SERVICE_NAME}=${TOTAL_REPLICAS}
        exit 1
    fi

    sleep $HEALTH_CHECK_INTERVAL
    ELAPSED=$((ELAPSED + HEALTH_CHECK_INTERVAL))
done

echo ""
echo "[4/5] Canary validation successful!"
echo ""
read -p "Do you want to promote canary to production? (yes/no): " PROMOTE

if [ "$PROMOTE" = "yes" ]; then
    echo ""
    echo "[5/5] Promoting canary to production..."

    # Update stable service to new version
    docker service update --image ${DOCKER_REGISTRY:-localhost:5000}/${SERVICE_NAME}:${NEW_VERSION} ${SERVICE_NAME}

    # Wait for update to complete
    sleep 10

    # Scale back to full capacity
    docker service scale ${SERVICE_NAME}=${TOTAL_REPLICAS}

    # Remove canary service
    docker service rm ${CANARY_SERVICE}

    echo ""
    echo "=================================================="
    echo "  ✓ CANARY DEPLOYMENT SUCCESSFUL"
    echo "=================================================="
    echo "Service $SERVICE_NAME updated to version $NEW_VERSION"
    echo "All $TOTAL_REPLICAS replicas are running the new version"
    echo "=================================================="
else
    echo ""
    echo "[5/5] Canary deployment cancelled - Rolling back..."
    docker service rm ${CANARY_SERVICE}
    docker service scale ${SERVICE_NAME}=${TOTAL_REPLICAS}

    echo ""
    echo "=================================================="
    echo "  CANARY DEPLOYMENT CANCELLED"
    echo "=================================================="
    echo "Service $SERVICE_NAME remains on old version"
    echo "All $TOTAL_REPLICAS replicas are running"
    echo "=================================================="
fi
