#!/bin/bash

# Deploy to Specific Environment
# Simplified deployment script for any environment

set -e

ENVIRONMENT=${1:-dev}
STRATEGY=${2:-rolling}

echo "=================================================="
echo "  DEPLOYING TO $ENVIRONMENT ENVIRONMENT"
echo "=================================================="
echo "Strategy: $STRATEGY"
echo "Timestamp: $(date)"
echo "=================================================="

case $ENVIRONMENT in
    dev)
        echo ""
        echo "Deploying to Development..."
        docker-compose -f docker-compose-dev.yml down
        docker-compose -f docker-compose-dev.yml up -d

        echo ""
        echo "Waiting for services to start (60 seconds)..."
        sleep 60

        echo ""
        echo "Running smoke tests..."
        bash scripts/testing/smoke-test.sh dev http://localhost
        ;;

    staging)
        echo ""
        echo "Deploying to Staging..."
        docker-compose -f docker-compose-staging.yml down
        docker-compose -f docker-compose-staging.yml up -d

        echo ""
        echo "Waiting for services to start (90 seconds)..."
        sleep 90

        echo ""
        echo "Running smoke tests..."
        bash scripts/testing/smoke-test.sh staging http://localhost

        echo ""
        read -p "Run load tests? (yes/no): " RUN_LOAD_TESTS
        if [ "$RUN_LOAD_TESTS" = "yes" ]; then
            echo "Running load tests..."
            bash scripts/testing/load-test.sh http://localhost:8180/api/genres 50 1000
        fi
        ;;

    prod)
        echo ""
        echo "Deploying to Production..."

        if ! docker node ls &> /dev/null; then
            echo "Error: Docker Swarm is not initialized"
            echo "Run: docker swarm init"
            exit 1
        fi

        export VERSION=${VERSION:-latest}
        export DOCKER_REGISTRY=${DOCKER_REGISTRY:-localhost:5000}

        if [ "$STRATEGY" = "rolling" ]; then
            echo "Using Rolling Update strategy..."
            docker stack deploy -c docker-swarm-stack.yml lms

        elif [ "$STRATEGY" = "blue-green" ]; then
            echo "Using Blue-Green deployment strategy..."
            SERVICES=(
                "genre-service"
                "author-service"
                "book-command-service"
                "book-query-service"
                "lending-service"
                "reader-service"
                "saga-orchestrator"
            )

            for service in "${SERVICES[@]}"; do
                bash scripts/deployment/blue-green-deploy.sh $service $VERSION 3
            done

        elif [ "$STRATEGY" = "canary" ]; then
            echo "Using Canary deployment strategy..."
            SERVICES=(
                "genre-service"
                "author-service"
                "book-command-service"
                "book-query-service"
                "lending-service"
                "reader-service"
                "saga-orchestrator"
            )

            for service in "${SERVICES[@]}"; do
                bash scripts/deployment/canary-deploy.sh $service $VERSION 1 3
            done

        else
            echo "Error: Unknown strategy: $STRATEGY"
            echo "Valid strategies: rolling, blue-green, canary"
            exit 1
        fi

        echo ""
        echo "Waiting for deployment to stabilize (60 seconds)..."
        sleep 60

        echo ""
        echo "Verifying deployment..."
        docker stack services lms
        docker stack ps lms --no-trunc

        echo ""
        echo "Monitoring health checks (30 seconds)..."
        sleep 30

        echo ""
        echo "Checking service health..."
        UNHEALTHY=0
        for service in $(docker stack services lms --format "{{.Name}}"); do
            REPLICAS=$(docker service ls --filter "name=$service" --format "{{.Replicas}}")
            RUNNING=$(echo $REPLICAS | cut -d'/' -f1)
            TOTAL=$(echo $REPLICAS | cut -d'/' -f2)

            if [ "$RUNNING" -eq "$TOTAL" ]; then
                echo "✓ $service: $REPLICAS"
            else
                echo "✗ $service: $REPLICAS (UNHEALTHY)"
                UNHEALTHY=$((UNHEALTHY + 1))
            fi
        done

        if [ $UNHEALTHY -gt 0 ]; then
            echo ""
            echo "⚠️  WARNING: $UNHEALTHY services are unhealthy!"
            echo "Consider rolling back with: docker stack rm lms"
            exit 1
        fi
        ;;

    *)
        echo "Error: Unknown environment: $ENVIRONMENT"
        echo "Valid environments: dev, staging, prod"
        exit 1
        ;;
esac

# Success
echo ""
echo "=================================================="
echo "  ✓ DEPLOYMENT SUCCESSFUL"
echo "=================================================="
echo "Environment: $ENVIRONMENT"
echo "Strategy: $STRATEGY"
echo "Timestamp: $(date)"
echo "=================================================="

case $ENVIRONMENT in
    dev)
        echo ""
        echo "Services available at:"
        echo "  - Genre Service: http://localhost:8080"
        echo "  - Author Service: http://localhost:8082"
        echo "  - Book Command: http://localhost:8083"
        echo "  - Book Query: http://localhost:8085"
        echo "  - Lending Service: http://localhost:8086"
        echo "  - Reader Service: http://localhost:8087"
        echo "  - Saga Orchestrator: http://localhost:8084"
        ;;
    staging)
        echo ""
        echo "Services available via Traefik:"
        echo "  - API Gateway: http://localhost:8180"
        echo "  - Traefik Dashboard: http://localhost:8190"
        ;;
    prod)
        echo ""
        echo "Services available via Docker Swarm:"
        echo "  - API Gateway: http://localhost (port 80)"
        echo "  - Traefik Dashboard: http://localhost:8080"
        echo ""
        echo "Monitor with:"
        echo "  docker stack services lms"
        echo "  docker service logs lms_genre-service -f"
        ;;
esac

echo "=================================================="
