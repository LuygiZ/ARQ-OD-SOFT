#!/bin/bash

# Quick Load Test using curl
# Simple and reliable

TARGET_URL=$1
REQUESTS=${2:-100}

if [ -z "$TARGET_URL" ]; then
    echo "Usage: $0 <target-url> [requests]"
    echo "Example: $0 http://localhost:8080/api/genres 100"
    exit 1
fi

echo "=================================================="
echo "  QUICK LOAD TEST"
echo "=================================================="
echo "Target: $TARGET_URL"
echo "Requests: $REQUESTS"
echo "=================================================="
echo ""

START_TIME=$(date +%s)

SUCCESS=0
FAILED=0
TOTAL_TIME=0
MIN_TIME=999999
MAX_TIME=0

declare -a TIMES

echo "Running requests..."

for ((i=1; i<=$REQUESTS; i++)); do
    # Measure request time
    START=$(date +%s%N)
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$TARGET_URL")
    END=$(date +%s%N)

    TIME_MS=$(( ($END - $START) / 1000000 ))

    if [ "$HTTP_CODE" = "200" ]; then
        SUCCESS=$((SUCCESS + 1))
        TOTAL_TIME=$((TOTAL_TIME + TIME_MS))
        TIMES+=($TIME_MS)

        if [ $TIME_MS -lt $MIN_TIME ]; then
            MIN_TIME=$TIME_MS
        fi
        if [ $TIME_MS -gt $MAX_TIME ]; then
            MAX_TIME=$TIME_MS
        fi
    else
        FAILED=$((FAILED + 1))
    fi

    # Progress indicator
    if [ $((i % 10)) -eq 0 ]; then
        printf "\rProgress: %d/%d (Success: %d, Failed: %d)" $i $REQUESTS $SUCCESS $FAILED
    fi
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo ""

# Calculate statistics
if [ $SUCCESS -gt 0 ]; then
    AVG_TIME=$((TOTAL_TIME / SUCCESS))

    # Sort times for percentiles
    IFS=$'\n' SORTED=($(sort -n <<<"${TIMES[*]}"))
    unset IFS

    P50_IDX=$((SUCCESS / 2))
    P95_IDX=$((SUCCESS * 95 / 100))
    P99_IDX=$((SUCCESS * 99 / 100))

    P50=${SORTED[$P50_IDX]:-0}
    P95=${SORTED[$P95_IDX]:-0}
    P99=${SORTED[$P99_IDX]:-0}

    if [ $DURATION -gt 0 ]; then
        RPS=$((SUCCESS / DURATION))
    else
        RPS=$SUCCESS
    fi
else
    AVG_TIME=0
    P50=0
    P95=0
    P99=0
    RPS=0
fi

# Results
echo "=================================================="
echo "  RESULTS"
echo "=================================================="
echo ""
echo "Total Requests:    $REQUESTS"
echo "Successful:        $SUCCESS"
echo "Failed:            $FAILED"
echo "Duration:          ${DURATION}s"
echo ""
echo "Response Times:"
echo "  Average:         ${AVG_TIME}ms"
echo "  Minimum:         ${MIN_TIME}ms"
echo "  Maximum:         ${MAX_TIME}ms"
echo "  50th percentile: ${P50}ms"
echo "  95th percentile: ${P95}ms"
echo "  99th percentile: ${P99}ms"
echo ""
echo "Throughput:        ${RPS} req/sec"
echo ""
echo "=================================================="
echo "  EVALUATION"
echo "=================================================="

# Evaluation
if [ $FAILED -eq 0 ]; then
    echo "✓ Reliability: 100% success rate"
else
    FAIL_RATE=$((FAILED * 100 / REQUESTS))
    if [ $FAIL_RATE -lt 5 ]; then
        echo "⚠ Reliability: ${FAIL_RATE}% failure rate (acceptable)"
    else
        echo "✗ Reliability: ${FAIL_RATE}% failure rate (poor)"
    fi
fi

if [ $RPS -gt 50 ]; then
    echo "✓ Throughput: Excellent ($RPS req/sec)"
elif [ $RPS -gt 20 ]; then
    echo "⚠ Throughput: Good ($RPS req/sec)"
else
    echo "✗ Throughput: Low ($RPS req/sec)"
fi

if [ $P95 -lt 200 ]; then
    echo "✓ Response Time: Excellent (P95: ${P95}ms)"
elif [ $P95 -lt 500 ]; then
    echo "✓ Response Time: Good (P95: ${P95}ms)"
elif [ $P95 -lt 1000 ]; then
    echo "⚠ Response Time: Acceptable (P95: ${P95}ms)"
else
    echo "✗ Response Time: Poor (P95: ${P95}ms)"
fi

echo "=================================================="
