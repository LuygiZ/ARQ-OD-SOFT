#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# ODSOFT 2025/2026 - Student C
# Automatic Rollback Script (Criterion 4.2)
# ═══════════════════════════════════════════════════════════════════════════════

set -e

# Configuration
SERVICE_NAME="${1:-lending-service}"
ENVIRONMENT="${2:-production}"
HEALTH_CHECK_URL="${3:-http://localhost:8086/actuator/health}"
MAX_HEALTH_RETRIES=5
HEALTH_CHECK_INTERVAL=10

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  ODSOFT - Automatic Rollback"
echo "  Service: ${SERVICE_NAME}"
echo "  Environment: ${ENVIRONMENT}"
echo "  Health Check URL: ${HEALTH_CHECK_URL}"
echo "═══════════════════════════════════════════════════════════════════════════════"

# Function to check service health
check_health() {
    local retries=0

    while [ $retries -lt $MAX_HEALTH_RETRIES ]; do
        echo "Health check attempt $((retries + 1))/${MAX_HEALTH_RETRIES}..."

        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${HEALTH_CHECK_URL}" || echo "000")

        if [ "$HTTP_CODE" = "200" ]; then
            echo "✅ Health check passed (HTTP ${HTTP_CODE})"
            return 0
        fi

        echo "⚠️ Health check failed (HTTP ${HTTP_CODE})"
        retries=$((retries + 1))

        if [ $retries -lt $MAX_HEALTH_RETRIES ]; then
            echo "Waiting ${HEALTH_CHECK_INTERVAL}s before retry..."
            sleep $HEALTH_CHECK_INTERVAL
        fi
    done

    echo "❌ Health check failed after ${MAX_HEALTH_RETRIES} attempts"
    return 1
}

# Function to get previous image version
get_previous_version() {
    # Get all image tags sorted by creation date, skip 'latest' and current
    docker images "${SERVICE_NAME}" --format "{{.Tag}}" | \
        grep -v "latest" | \
        sort -rn | \
        head -2 | \
        tail -1
}

# Function to perform rollback
perform_rollback() {
    local previous_version=$(get_previous_version)

    if [ -z "$previous_version" ]; then
        echo "❌ No previous version found for rollback!"
        exit 1
    fi

    echo "🔄 Rolling back to version: ${previous_version}"

    # Stop current containers
    echo "Stopping current containers..."
    docker stop $(docker ps -q --filter "name=${SERVICE_NAME}-${ENVIRONMENT}") 2>/dev/null || true
    docker rm $(docker ps -aq --filter "name=${SERVICE_NAME}-${ENVIRONMENT}") 2>/dev/null || true

    # Start previous version
    echo "Starting previous version..."
    docker run -d \
        --name "${SERVICE_NAME}-${ENVIRONMENT}-rollback" \
        -p 8086:8086 \
        -e SPRING_PROFILES_ACTIVE=${ENVIRONMENT} \
        --restart unless-stopped \
        "${SERVICE_NAME}:${previous_version}"

    # Verify rollback
    echo "Waiting for rollback to complete..."
    sleep 30

    if check_health; then
        echo "✅ Rollback successful to version ${previous_version}"

        # Record rollback event
        echo "{\"event\": \"rollback\", \"service\": \"${SERVICE_NAME}\", \"environment\": \"${ENVIRONMENT}\", \"previous_version\": \"${previous_version}\", \"timestamp\": \"$(date -Iseconds)\"}" >> rollback-history.json

        return 0
    else
        echo "❌ Rollback failed - service is still unhealthy"
        return 1
    fi
}

# Main function
main() {
    echo ""
    echo "Checking current service health..."

    if check_health; then
        echo ""
        echo "✅ Service is healthy - no rollback needed"
        exit 0
    fi

    echo ""
    echo "❌ Service is unhealthy - initiating automatic rollback..."
    echo ""

    if perform_rollback; then
        echo ""
        echo "═══════════════════════════════════════════════════════════════════════════════"
        echo "  ✅ ROLLBACK COMPLETED SUCCESSFULLY"
        echo "═══════════════════════════════════════════════════════════════════════════════"

        # Send notification (if configured)
        if [ -n "$NOTIFICATION_WEBHOOK" ]; then
            curl -X POST "$NOTIFICATION_WEBHOOK" \
                -H "Content-Type: application/json" \
                -d "{\"text\": \"🔄 Automatic rollback executed for ${SERVICE_NAME} in ${ENVIRONMENT}\"}" \
                2>/dev/null || true
        fi

        exit 0
    else
        echo ""
        echo "═══════════════════════════════════════════════════════════════════════════════"
        echo "  ❌ ROLLBACK FAILED - MANUAL INTERVENTION REQUIRED"
        echo "═══════════════════════════════════════════════════════════════════════════════"
        exit 1
    fi
}

# Run main
main
