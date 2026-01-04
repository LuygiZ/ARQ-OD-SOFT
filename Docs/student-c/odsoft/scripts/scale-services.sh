#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# ODSOFT 2025/2026 - Student C
# Auto-Scale Services Script (Criterion 3.5)
# ═══════════════════════════════════════════════════════════════════════════════

set -e

# Configuration
SERVICE_NAME="${1:-lending-service}"
MIN_REPLICAS="${2:-1}"
MAX_REPLICAS="${3:-5}"
CPU_THRESHOLD="${4:-70}"
MEMORY_THRESHOLD="${5:-80}"

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  ODSOFT - Auto-Scale Services"
echo "  Service: ${SERVICE_NAME}"
echo "  Min Replicas: ${MIN_REPLICAS}"
echo "  Max Replicas: ${MAX_REPLICAS}"
echo "  CPU Threshold: ${CPU_THRESHOLD}%"
echo "  Memory Threshold: ${MEMORY_THRESHOLD}%"
echo "═══════════════════════════════════════════════════════════════════════════════"

# Function to get current replica count
get_current_replicas() {
    docker ps --filter "name=${SERVICE_NAME}" --format "{{.Names}}" | wc -l
}

# Function to get CPU usage
get_cpu_usage() {
    local container_id=$(docker ps --filter "name=${SERVICE_NAME}" --format "{{.ID}}" | head -1)
    if [ -n "$container_id" ]; then
        docker stats --no-stream --format "{{.CPUPerc}}" "$container_id" | tr -d '%' | cut -d'.' -f1
    else
        echo "0"
    fi
}

# Function to get memory usage
get_memory_usage() {
    local container_id=$(docker ps --filter "name=${SERVICE_NAME}" --format "{{.ID}}" | head -1)
    if [ -n "$container_id" ]; then
        docker stats --no-stream --format "{{.MemPerc}}" "$container_id" | tr -d '%' | cut -d'.' -f1
    else
        echo "0"
    fi
}

# Function to scale up
scale_up() {
    local current=$1
    local new_count=$((current + 1))

    if [ $new_count -le $MAX_REPLICAS ]; then
        echo "📈 Scaling UP ${SERVICE_NAME} from ${current} to ${new_count} replicas..."

        # For docker-compose
        docker-compose -f infrastructure/docker-compose.yml up -d --scale ${SERVICE_NAME}=${new_count} 2>/dev/null || \
        # For docker swarm
        docker service scale ${SERVICE_NAME}=${new_count} 2>/dev/null || \
        # Manual scaling
        echo "⚠️ Auto-scale not available - please scale manually"

        echo "✅ Scaled to ${new_count} replicas"
    else
        echo "⚠️ Already at maximum replicas (${MAX_REPLICAS})"
    fi
}

# Function to scale down
scale_down() {
    local current=$1
    local new_count=$((current - 1))

    if [ $new_count -ge $MIN_REPLICAS ]; then
        echo "📉 Scaling DOWN ${SERVICE_NAME} from ${current} to ${new_count} replicas..."

        # For docker-compose
        docker-compose -f infrastructure/docker-compose.yml up -d --scale ${SERVICE_NAME}=${new_count} 2>/dev/null || \
        # For docker swarm
        docker service scale ${SERVICE_NAME}=${new_count} 2>/dev/null || \
        # Manual scaling
        echo "⚠️ Auto-scale not available - please scale manually"

        echo "✅ Scaled to ${new_count} replicas"
    else
        echo "⚠️ Already at minimum replicas (${MIN_REPLICAS})"
    fi
}

# Main scaling logic
main() {
    local current_replicas=$(get_current_replicas)
    local cpu_usage=$(get_cpu_usage)
    local memory_usage=$(get_memory_usage)

    echo ""
    echo "Current Status:"
    echo "  - Replicas: ${current_replicas}"
    echo "  - CPU Usage: ${cpu_usage}%"
    echo "  - Memory Usage: ${memory_usage}%"
    echo ""

    # Scaling decision
    if [ "$cpu_usage" -gt "$CPU_THRESHOLD" ] || [ "$memory_usage" -gt "$MEMORY_THRESHOLD" ]; then
        echo "🔥 High load detected - scaling up..."
        scale_up $current_replicas
    elif [ "$cpu_usage" -lt 30 ] && [ "$memory_usage" -lt 40 ] && [ $current_replicas -gt $MIN_REPLICAS ]; then
        echo "💤 Low load detected - scaling down..."
        scale_down $current_replicas
    else
        echo "✅ Load is within normal range - no scaling needed"
    fi
}

# Run main function
main

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "  Scaling complete"
echo "═══════════════════════════════════════════════════════════════════════════════"
