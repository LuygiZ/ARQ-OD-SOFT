#!/bin/bash

# Auto-scaling Script for Docker Swarm Services
# Scales services based on CPU and memory metrics

set -e

SERVICE_NAME=$1
MIN_REPLICAS=${2:-2}
MAX_REPLICAS=${3:-5}
CPU_THRESHOLD=${4:-70}
MEMORY_THRESHOLD=${5:-80}

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: $0 <service-name> [min-replicas] [max-replicas] [cpu-threshold-%] [memory-threshold-%]"
    echo "Example: $0 genre-service 2 5 70 80"
    exit 1
fi

echo "=================================================="
echo "  AUTO-SCALING MONITOR"
echo "=================================================="
echo "Service: $SERVICE_NAME"
echo "Min Replicas: $MIN_REPLICAS"
echo "Max Replicas: $MAX_REPLICAS"
echo "CPU Threshold: $CPU_THRESHOLD%"
echo "Memory Threshold: $MEMORY_THRESHOLD%"
echo "=================================================="

# Function to get current replica count
get_replica_count() {
    docker service ls --filter "name=$SERVICE_NAME" --format "{{.Replicas}}" | cut -d'/' -f1
}

# Function to get average CPU usage
get_cpu_usage() {
    local service=$1
    local total_cpu=0
    local count=0

    for container in $(docker service ps $service --filter "desired-state=running" -q); do
        local container_id=$(docker inspect --format='{{.Status.ContainerStatus.ContainerID}}' $container 2>/dev/null || echo "")
        if [ -n "$container_id" ]; then
            local cpu=$(docker stats --no-stream --format "{{.CPUPerc}}" $container_id 2>/dev/null | sed 's/%//' || echo "0")
            if [ -n "$cpu" ] && [ "$cpu" != "0" ]; then
                total_cpu=$(echo "$total_cpu + $cpu" | bc)
                count=$((count + 1))
            fi
        fi
    done

    if [ $count -gt 0 ]; then
        echo "scale=2; $total_cpu / $count" | bc
    else
        echo "0"
    fi
}

# Function to get average memory usage
get_memory_usage() {
    local service=$1
    local total_mem=0
    local count=0

    for container in $(docker service ps $service --filter "desired-state=running" -q); do
        local container_id=$(docker inspect --format='{{.Status.ContainerStatus.ContainerID}}' $container 2>/dev/null || echo "")
        if [ -n "$container_id" ]; then
            local mem=$(docker stats --no-stream --format "{{.MemPerc}}" $container_id 2>/dev/null | sed 's/%//' || echo "0")
            if [ -n "$mem" ] && [ "$mem" != "0" ]; then
                total_mem=$(echo "$total_mem + $mem" | bc)
                count=$((count + 1))
            fi
        fi
    done

    if [ $count -gt 0 ]; then
        echo "scale=2; $total_mem / $count" | bc
    else
        echo "0"
    fi
}

# Function to scale service
scale_service() {
    local service=$1
    local target_replicas=$2

    echo ""
    echo "[ACTION] Scaling $service to $target_replicas replicas..."
    docker service scale ${service}=${target_replicas}

    # Wait for scaling to complete
    sleep 10

    echo "✓ Scaling complete!"
}

# Monitoring loop
echo ""
echo "Starting monitoring (Press Ctrl+C to stop)..."
echo ""

ITERATION=0
while true; do
    ITERATION=$((ITERATION + 1))
    CURRENT_REPLICAS=$(get_replica_count)

    echo "[$ITERATION] $(date '+%Y-%m-%d %H:%M:%S')"
    echo "  Current Replicas: $CURRENT_REPLICAS"

    # Get metrics
    AVG_CPU=$(get_cpu_usage $SERVICE_NAME)
    AVG_MEM=$(get_memory_usage $SERVICE_NAME)

    echo "  Average CPU: ${AVG_CPU}%"
    echo "  Average Memory: ${AVG_MEM}%"

    # Scaling decision
    SHOULD_SCALE_UP=false
    SHOULD_SCALE_DOWN=false

    # Check if we need to scale up
    if (( $(echo "$AVG_CPU > $CPU_THRESHOLD" | bc -l) )) || (( $(echo "$AVG_MEM > $MEMORY_THRESHOLD" | bc -l) )); then
        if [ $CURRENT_REPLICAS -lt $MAX_REPLICAS ]; then
            SHOULD_SCALE_UP=true
            TARGET_REPLICAS=$((CURRENT_REPLICAS + 1))
            REASON="High resource usage detected"
        fi
    fi

    # Check if we can scale down
    if (( $(echo "$AVG_CPU < $(echo "$CPU_THRESHOLD * 0.5" | bc)" | bc -l) )) && (( $(echo "$AVG_MEM < $(echo "$MEMORY_THRESHOLD * 0.5" | bc)" | bc -l) )); then
        if [ $CURRENT_REPLICAS -gt $MIN_REPLICAS ]; then
            SHOULD_SCALE_DOWN=true
            TARGET_REPLICAS=$((CURRENT_REPLICAS - 1))
            REASON="Low resource usage detected"
        fi
    fi

    # Perform scaling
    if [ "$SHOULD_SCALE_UP" = true ]; then
        echo "  → $REASON (CPU: ${AVG_CPU}%, Mem: ${AVG_MEM}%)"
        scale_service $SERVICE_NAME $TARGET_REPLICAS
    elif [ "$SHOULD_SCALE_DOWN" = true ]; then
        echo "  → $REASON (CPU: ${AVG_CPU}%, Mem: ${AVG_MEM}%)"
        scale_service $SERVICE_NAME $TARGET_REPLICAS
    else
        echo "  → No scaling needed"
    fi

    echo ""

    # Wait before next check
    sleep 30
done
