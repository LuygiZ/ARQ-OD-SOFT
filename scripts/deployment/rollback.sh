#!/bin/bash

# Automatic Rollback Script for Docker Swarm
# Monitors service health and triggers automatic rollback on failures

set -e

SERVICE_NAME=$1
HEALTH_CHECK_INTERVAL=${2:-10}
HEALTH_CHECK_RETRIES=${3:-3}

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: $0 <service-name> [check-interval-seconds] [max-retries]"
    echo "Example: $0 genre-service 10 3"
    exit 1
fi

echo "=================================================="
echo "  AUTOMATIC ROLLBACK MONITOR"
echo "=================================================="
echo "Service: $SERVICE_NAME"
echo "Health Check Interval: ${HEALTH_CHECK_INTERVAL}s"
echo "Max Failed Checks: $HEALTH_CHECK_RETRIES"
echo "=================================================="

# Function to check service health
check_service_health() {
    local service=$1

    # Get total and running replicas
    local replicas_info=$(docker service ls --filter "name=^${service}$" --format "{{.Replicas}}")

    if [ -z "$replicas_info" ]; then
        echo "✗ Service not found"
        return 1
    fi

    local running=$(echo $replicas_info | cut -d'/' -f1)
    local total=$(echo $replicas_info | cut -d'/' -f2)

    echo "Replicas: $running/$total"

    # Check if all replicas are running
    if [ "$running" -eq "$total" ] && [ "$total" -gt 0 ]; then
        # Additional check: verify containers are actually healthy
        local unhealthy=0

        for task_id in $(docker service ps $service --filter "desired-state=running" -q); do
            local task_state=$(docker inspect --format='{{.Status.State}}' $task_id 2>/dev/null || echo "unknown")

            if [ "$task_state" != "running" ]; then
                unhealthy=$((unhealthy + 1))
            fi
        done

        if [ $unhealthy -eq 0 ]; then
            echo "✓ Service is healthy"
            return 0
        else
            echo "✗ $unhealthy unhealthy tasks detected"
            return 1
        fi
    else
        echo "✗ Service is unhealthy ($running/$total replicas running)"
        return 1
    fi
}

# Function to perform rollback
perform_rollback() {
    local service=$1

    echo ""
    echo "=================================================="
    echo "  TRIGGERING AUTOMATIC ROLLBACK"
    echo "=================================================="
    echo "Service: $service"
    echo "Timestamp: $(date)"
    echo ""

    # Get previous version from service update history
    local previous_image=$(docker service inspect $service --format='{{range .PreviousSpec.TaskTemplate.ContainerSpec}}{{.Image}}{{end}}' 2>/dev/null || echo "")

    if [ -z "$previous_image" ]; then
        echo "✗ Cannot determine previous version"
        echo "Manual intervention required!"
        return 1
    fi

    echo "Rolling back to previous image: $previous_image"

    # Perform rollback
    docker service rollback $service

    echo ""
    echo "Rollback initiated. Monitoring recovery..."
    sleep 20

    # Verify rollback success
    if check_service_health $service; then
        echo ""
        echo "=================================================="
        echo "  ✓ ROLLBACK SUCCESSFUL"
        echo "=================================================="
        echo "Service: $service"
        echo "Image: $previous_image"
        echo "All replicas are healthy"
        echo "=================================================="
        return 0
    else
        echo ""
        echo "=================================================="
        echo "  ✗ ROLLBACK FAILED"
        echo "=================================================="
        echo "Service is still unhealthy after rollback"
        echo "CRITICAL: Manual intervention required!"
        echo "=================================================="
        return 1
    fi
}

# Main monitoring loop
echo ""
echo "Starting health monitoring..."
echo ""

FAILED_CHECKS=0
CHECK_COUNT=0

while true; do
    CHECK_COUNT=$((CHECK_COUNT + 1))
    echo "[Check #$CHECK_COUNT] $(date '+%Y-%m-%d %H:%M:%S')"

    if check_service_health $SERVICE_NAME; then
        FAILED_CHECKS=0
        echo "Status: OK"
    else
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        echo "Status: FAILED (${FAILED_CHECKS}/${HEALTH_CHECK_RETRIES})"

        if [ $FAILED_CHECKS -ge $HEALTH_CHECK_RETRIES ]; then
            echo ""
            echo "⚠️  Health check failed $FAILED_CHECKS times!"

            perform_rollback $SERVICE_NAME

            # Exit after rollback attempt
            exit $?
        fi
    fi

    echo ""
    sleep $HEALTH_CHECK_INTERVAL
done
