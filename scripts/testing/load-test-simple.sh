#!/bin/bash

# Simple Load Test using curl
# Works on any system with curl installed

TARGET_URL=$1
REQUESTS=${2:-100}
CONCURRENT=${3:-10}

if [ -z "$TARGET_URL" ]; then
    echo "Usage: $0 <target-url> [requests] [concurrent]"
    echo "Example: $0 http://localhost:8080/api/genres 100 10"
    exit 1
fi

echo "=================================================="
echo "  SIMPLE LOAD TEST"
echo "=================================================="
echo "Target URL: $TARGET_URL"
echo "Total Requests: $REQUESTS"
echo "Concurrent Requests: $CONCURRENT"
echo "=================================================="
echo ""

# Create results directory
REPORT_DIR="./load-test-results"
mkdir -p $REPORT_DIR
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/load-test-$TIMESTAMP.txt"

echo "Starting load test at $(date)" | tee $REPORT_FILE
echo "" | tee -a $REPORT_FILE

# Variables for statistics
TOTAL_TIME=0
SUCCESS_COUNT=0
FAILED_COUNT=0
MIN_TIME=99999
MAX_TIME=0

# Array to store response times
declare -a RESPONSE_TIMES

# Function to make request and measure time
make_request() {
    START=$(date +%s%N)
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$TARGET_URL" 2>/dev/null)
    END=$(date +%s%N)

    TIME_MS=$(( ($END - $START) / 1000000 ))

    echo "$HTTP_CODE $TIME_MS"
}

echo "Running $REQUESTS requests with $CONCURRENT concurrent..."
echo ""

# Progress bar
PROGRESS=0
BATCH_SIZE=$CONCURRENT

while [ $PROGRESS -lt $REQUESTS ]; do
    # Launch concurrent requests
    PIDS=()

    for ((i=0; i<$BATCH_SIZE && $PROGRESS<$REQUESTS; i++)); do
        make_request &
        PIDS+=($!)
        PROGRESS=$((PROGRESS + 1))
    done

    # Wait for batch to complete and collect results
    for pid in ${PIDS[@]}; do
        RESULT=$(wait $pid && echo "$?" || echo "1")

        # Read output from background job
        read -r HTTP_CODE TIME_MS <<< "$RESULT"

        if [ "$HTTP_CODE" = "200" ]; then
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            TOTAL_TIME=$((TOTAL_TIME + TIME_MS))
            RESPONSE_TIMES+=($TIME_MS)

            # Update min/max
            if [ $TIME_MS -lt $MIN_TIME ]; then
                MIN_TIME=$TIME_MS
            fi
            if [ $TIME_MS -gt $MAX_TIME ]; then
                MAX_TIME=$TIME_MS
            fi
        else
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    done

    # Show progress
    PERCENT=$((PROGRESS * 100 / REQUESTS))
    printf "\rProgress: %d/%d (%d%%) - Success: %d, Failed: %d" \
        $PROGRESS $REQUESTS $PERCENT $SUCCESS_COUNT $FAILED_COUNT
done

echo ""
echo ""

# Calculate statistics
if [ $SUCCESS_COUNT -gt 0 ]; then
    AVG_TIME=$((TOTAL_TIME / SUCCESS_COUNT))

    # Sort response times for percentile calculation
    IFS=$'\n' SORTED_TIMES=($(sort -n <<<"${RESPONSE_TIMES[*]}"))
    unset IFS

    # Calculate percentiles
    P50_IDX=$((SUCCESS_COUNT / 2))
    P95_IDX=$((SUCCESS_COUNT * 95 / 100))
    P99_IDX=$((SUCCESS_COUNT * 99 / 100))

    P50=${SORTED_TIMES[$P50_IDX]:-0}
    P95=${SORTED_TIMES[$P95_IDX]:-0}
    P99=${SORTED_TIMES[$P99_IDX]:-0}
else
    AVG_TIME=0
    P50=0
    P95=0
    P99=0
fi

# Calculate requests per second (rough estimate)
TOTAL_DURATION=$((MAX_TIME / 1000))
if [ $TOTAL_DURATION -gt 0 ]; then
    RPS=$((SUCCESS_COUNT / TOTAL_DURATION))
else
    RPS=$SUCCESS_COUNT
fi

# Display results
echo "=================================================="
echo "  LOAD TEST RESULTS"
echo "=================================================="
echo "" | tee -a $REPORT_FILE
echo "Total Requests: $REQUESTS" | tee -a $REPORT_FILE
echo "Successful: $SUCCESS_COUNT" | tee -a $REPORT_FILE
echo "Failed: $FAILED_COUNT" | tee -a $REPORT_FILE
echo "" | tee -a $REPORT_FILE
echo "Response Time Statistics:" | tee -a $REPORT_FILE
echo "  Average: ${AVG_TIME}ms" | tee -a $REPORT_FILE
echo "  Minimum: ${MIN_TIME}ms" | tee -a $REPORT_FILE
echo "  Maximum: ${MAX_TIME}ms" | tee -a $REPORT_FILE
echo "" | tee -a $REPORT_FILE
echo "Percentiles:" | tee -a $REPORT_FILE
echo "  50th: ${P50}ms" | tee -a $REPORT_FILE
echo "  95th: ${P95}ms" | tee -a $REPORT_FILE
echo "  99th: ${P99}ms" | tee -a $REPORT_FILE
echo "" | tee -a $REPORT_FILE
echo "Throughput: ~${RPS} req/sec" | tee -a $REPORT_FILE
echo "=================================================="
echo ""

# Performance evaluation
echo "Performance Evaluation:" | tee -a $REPORT_FILE

if [ $FAILED_COUNT -eq 0 ]; then
    echo "  ✓ No failed requests" | tee -a $REPORT_FILE
else
    FAIL_RATE=$((FAILED_COUNT * 100 / REQUESTS))
    echo "  ✗ $FAILED_COUNT failed requests (${FAIL_RATE}%)" | tee -a $REPORT_FILE
fi

if [ $RPS -gt 50 ]; then
    echo "  ✓ Good throughput ($RPS req/sec)" | tee -a $REPORT_FILE
else
    echo "  ⚠ Low throughput ($RPS req/sec)" | tee -a $REPORT_FILE
fi

if [ $P95 -lt 500 ]; then
    echo "  ✓ Good response time (95th: ${P95}ms)" | tee -a $REPORT_FILE
elif [ $P95 -lt 1000 ]; then
    echo "  ⚠ Acceptable response time (95th: ${P95}ms)" | tee -a $REPORT_FILE
else
    echo "  ✗ Poor response time (95th: ${P95}ms)" | tee -a $REPORT_FILE
fi

echo ""
echo "Full report saved to: $REPORT_FILE"
echo "=================================================="
