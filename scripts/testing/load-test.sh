#!/bin/bash

# Load Testing Script using Apache Bench
# Tests system performance under load and generates reports

set -e

TARGET_URL=$1
CONCURRENT_USERS=${2:-50}
TOTAL_REQUESTS=${3:-1000}
REPORT_DIR=${4:-"./load-test-results"}

if [ -z "$TARGET_URL" ]; then
    echo "Usage: $0 <target-url> [concurrent-users] [total-requests] [report-dir]"
    echo "Example: $0 http://localhost/api/genres 50 1000"
    exit 1
fi

echo "=================================================="
echo "  LOAD TESTING"
echo "=================================================="
echo "Target URL: $TARGET_URL"
echo "Concurrent Users: $CONCURRENT_USERS"
echo "Total Requests: $TOTAL_REQUESTS"
echo "Report Directory: $REPORT_DIR"
echo "=================================================="

# Create report directory
mkdir -p $REPORT_DIR
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/load-test-$TIMESTAMP.txt"

# Check if Apache Bench is installed
if ! command -v ab &> /dev/null; then
    echo "Error: Apache Bench (ab) is not installed"
    echo "Install it with: sudo apt-get install apache2-utils"
    exit 1
fi

echo ""
echo "Starting load test at $(date)..."
echo ""

# Run Apache Bench
ab -n $TOTAL_REQUESTS -c $CONCURRENT_USERS -g "$REPORT_DIR/gnuplot-$TIMESTAMP.tsv" "$TARGET_URL" > "$REPORT_FILE"

# Display results
echo ""
echo "=================================================="
echo "  LOAD TEST RESULTS"
echo "=================================================="
echo ""

# Extract key metrics
REQUESTS_PER_SEC=$(grep "Requests per second" "$REPORT_FILE" | awk '{print $4}')
TIME_PER_REQUEST=$(grep "Time per request" "$REPORT_FILE" | grep "mean" | awk '{print $4}')
FAILED_REQUESTS=$(grep "Failed requests" "$REPORT_FILE" | awk '{print $3}')
P50=$(grep "50%" "$REPORT_FILE" | awk '{print $2}')
P95=$(grep "95%" "$REPORT_FILE" | awk '{print $2}')
P99=$(grep "99%" "$REPORT_FILE" | awk '{print $2}')

echo "Requests per second: $REQUESTS_PER_SEC"
echo "Time per request (mean): ${TIME_PER_REQUEST}ms"
echo "Failed requests: $FAILED_REQUESTS"
echo ""
echo "Response Time Percentiles:"
echo "  50th percentile: ${P50}ms"
echo "  95th percentile: ${P95}ms"
echo "  99th percentile: ${P99}ms"
echo ""
echo "=================================================="
echo "Full report saved to: $REPORT_FILE"
echo "=================================================="

# Performance evaluation
echo ""
echo "Performance Evaluation:"

FAILED_REQUESTS_INT=$(echo "$FAILED_REQUESTS" | sed 's/[^0-9]*//g')
if [ -z "$FAILED_REQUESTS_INT" ]; then
    FAILED_REQUESTS_INT=0
fi

if [ $FAILED_REQUESTS_INT -eq 0 ]; then
    echo "  ✓ No failed requests"
else
    echo "  ✗ $FAILED_REQUESTS_INT failed requests detected"
fi

RPS_INT=$(echo "$REQUESTS_PER_SEC" | cut -d'.' -f1)
if [ $RPS_INT -gt 100 ]; then
    echo "  ✓ Good throughput ($REQUESTS_PER_SEC req/sec)"
elif [ $RPS_INT -gt 50 ]; then
    echo "  ⚠ Moderate throughput ($REQUESTS_PER_SEC req/sec)"
else
    echo "  ✗ Low throughput ($REQUESTS_PER_SEC req/sec)"
fi

P95_INT=$(echo "$P95" | cut -d'.' -f1)
if [ $P95_INT -lt 500 ]; then
    echo "  ✓ Good response time (95th: ${P95}ms)"
elif [ $P95_INT -lt 1000 ]; then
    echo "  ⚠ Acceptable response time (95th: ${P95}ms)"
else
    echo "  ✗ Poor response time (95th: ${P95}ms)"
fi

echo ""

# Generate comparison if previous test exists
PREVIOUS_TEST=$(ls -t $REPORT_DIR/load-test-*.txt 2>/dev/null | sed -n '2p')
if [ -n "$PREVIOUS_TEST" ]; then
    echo "=================================================="
    echo "  COMPARISON WITH PREVIOUS TEST"
    echo "=================================================="

    PREV_RPS=$(grep "Requests per second" "$PREVIOUS_TEST" | awk '{print $4}')
    PREV_P95=$(grep "95%" "$PREVIOUS_TEST" | awk '{print $2}')

    echo "Requests per second:"
    echo "  Previous: $PREV_RPS"
    echo "  Current: $REQUESTS_PER_SEC"

    IMPROVEMENT=$(echo "scale=2; (($REQUESTS_PER_SEC - $PREV_RPS) / $PREV_RPS) * 100" | bc)
    if (( $(echo "$IMPROVEMENT > 0" | bc -l) )); then
        echo "  → Improvement: +${IMPROVEMENT}%"
    else
        echo "  → Regression: ${IMPROVEMENT}%"
    fi

    echo ""
    echo "95th Percentile Response Time:"
    echo "  Previous: ${PREV_P95}ms"
    echo "  Current: ${P95}ms"

    echo ""
    echo "=================================================="
fi

echo ""
echo "Load test completed at $(date)"
