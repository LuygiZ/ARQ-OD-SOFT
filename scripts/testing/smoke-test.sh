#!/bin/bash

# Smoke Testing Script
# Performs basic health checks on all services

set -e

ENVIRONMENT=${1:-"dev"}
BASE_URL=${2:-"http://localhost"}

echo "=================================================="
echo "  SMOKE TESTS - $ENVIRONMENT Environment"
echo "=================================================="
echo "Base URL: $BASE_URL"
echo "Timestamp: $(date)"
echo "=================================================="

# Define services and their ports based on environment
declare -A SERVICES

if [ "$ENVIRONMENT" = "dev" ]; then
    SERVICES=(
        ["genre-service"]="8080"
        ["author-service"]="8082"
        ["book-command-service"]="8083"
        ["book-query-service"]="8085"
        ["lending-service"]="8086"
        ["reader-service"]="8087"
        ["saga-orchestrator"]="8084"
    )
elif [ "$ENVIRONMENT" = "staging" ]; then
    # Staging uses Traefik, all services on port 8180
    SERVICES=(
        ["genres"]="8180"
        ["authors"]="8180"
        ["books"]="8180"
        ["lendings"]="8180"
        ["readers"]="8180"
        ["catalog"]="8180"
    )
else
    # Production uses standard port 80
    SERVICES=(
        ["genres"]="80"
        ["authors"]="80"
        ["books"]="80"
        ["lendings"]="80"
        ["readers"]="80"
        ["catalog"]="80"
    )
fi

PASSED=0
FAILED=0
WARNINGS=0

# Function to test endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}

    echo -n "Testing $name... "

    # Make HTTP request
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" 2>/dev/null || echo "000")

    if [ "$HTTP_CODE" = "$expected_status" ]; then
        echo "✓ PASS (HTTP $HTTP_CODE)"
        PASSED=$((PASSED + 1))
        return 0
    elif [ "$HTTP_CODE" = "000" ]; then
        echo "✗ FAIL (Connection timeout or refused)"
        FAILED=$((FAILED + 1))
        return 1
    else
        echo "⚠ WARNING (HTTP $HTTP_CODE, expected $expected_status)"
        WARNINGS=$((WARNINGS + 1))
        return 2
    fi
}

# Function to test health endpoint
test_health() {
    local service=$1
    local port=$2

    local health_url="$BASE_URL:$port/actuator/health"

    if [ "$ENVIRONMENT" = "staging" ] || [ "$ENVIRONMENT" = "prod" ]; then
        # For staging/prod with Traefik
        health_url="$BASE_URL:$port/api/$service/actuator/health"
    fi

    test_endpoint "$service Health Check" "$health_url" 200
}

echo ""
echo "Running health checks..."
echo ""

# Test each service
for service in "${!SERVICES[@]}"; do
    port=${SERVICES[$service]}
    test_health "$service" "$port"
done

# Additional endpoint tests for dev environment
if [ "$ENVIRONMENT" = "dev" ]; then
    echo ""
    echo "Running API endpoint tests..."
    echo ""

    test_endpoint "Genre Service - Swagger UI" "$BASE_URL:8080/swagger-ui/index.html" 200
    test_endpoint "Author Service - Swagger UI" "$BASE_URL:8082/swagger-ui/index.html" 200
    test_endpoint "Book Command Service - Swagger UI" "$BASE_URL:8083/swagger-ui/index.html" 200
    test_endpoint "Book Query Service - Swagger UI" "$BASE_URL:8085/swagger-ui/index.html" 200
fi

# Database connectivity test (dev environment only)
if [ "$ENVIRONMENT" = "dev" ]; then
    echo ""
    echo "Testing infrastructure..."
    echo ""

    # Test PostgreSQL
    if command -v pg_isready &> /dev/null; then
        if pg_isready -h localhost -p 5432 &> /dev/null; then
            echo "✓ PostgreSQL is accessible"
            PASSED=$((PASSED + 1))
        else
            echo "✗ PostgreSQL is not accessible"
            FAILED=$((FAILED + 1))
        fi
    fi

    # Test Redis
    if command -v redis-cli &> /dev/null; then
        if redis-cli -h localhost -p 6379 ping &> /dev/null; then
            echo "✓ Redis is accessible"
            PASSED=$((PASSED + 1))
        else
            echo "✗ Redis is not accessible"
            FAILED=$((FAILED + 1))
        fi
    fi

    # Test RabbitMQ
    RABBITMQ_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:15672 2>/dev/null || echo "000")
    if [ "$RABBITMQ_STATUS" = "200" ]; then
        echo "✓ RabbitMQ Management UI is accessible"
        PASSED=$((PASSED + 1))
    else
        echo "✗ RabbitMQ Management UI is not accessible"
        FAILED=$((FAILED + 1))
    fi
fi

# Summary
echo ""
echo "=================================================="
echo "  SMOKE TEST SUMMARY"
echo "=================================================="
echo "Total Tests: $((PASSED + FAILED + WARNINGS))"
echo "  ✓ Passed: $PASSED"
echo "  ⚠ Warnings: $WARNINGS"
echo "  ✗ Failed: $FAILED"
echo "=================================================="

if [ $FAILED -gt 0 ]; then
    echo "Result: FAILED"
    echo "=================================================="
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo "Result: PASSED WITH WARNINGS"
    echo "=================================================="
    exit 0
else
    echo "Result: ALL TESTS PASSED"
    echo "=================================================="
    exit 0
fi
