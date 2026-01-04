#!/bin/bash

##############################################################################
# Performance Testing Automation Script
#
# Runs Locust performance tests with different load levels and generates reports
#
# EVALUATION CRITERIA COVERAGE:
# - 2.4: Performance/load testing with multiple concurrent users
# - Demonstrates scalability testing (50, 100, 500, 1000 users)
# - Generates comparison data for analysis
#
# HOW TO USE:
# 1. Make executable: chmod +x run-performance-tests.sh
# 2. Run: ./run-performance-tests.sh
# 3. Results will be in: results/
##############################################################################

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
HOST="http://localhost:8080"
RESULTS_DIR="results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create results directory
mkdir -p "$RESULTS_DIR"

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}MICROSERVICES PERFORMANCE TEST SUITE${NC}"
echo -e "${GREEN}======================================${NC}"
echo ""

# Check if Locust is installed
if ! command -v locust &> /dev/null; then
    echo -e "${RED}ERROR: Locust is not installed${NC}"
    echo "Install with: pip install -r requirements.txt"
    exit 1
fi

# Check if microservices are running
echo -e "${YELLOW}Checking if services are running...${NC}"
if ! curl -s "http://localhost:8080/api/genres" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Genre Service is not running on port 8080${NC}"
    exit 1
fi
if ! curl -s "http://localhost:8082/api/authors" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Author Service is not running on port 8082${NC}"
    exit 1
fi
if ! curl -s "http://localhost:8083/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Book Command Service is not running on port 8083${NC}"
    exit 1
fi
if ! curl -s "http://localhost:8084/actuator/health" > /dev/null 2>&1; then
    echo -e "${YELLOW}WARNING: Saga Orchestrator might not be running on port 8084${NC}"
fi

echo -e "${GREEN}All services are running!${NC}"
echo ""

##############################################################################
# TEST 1: Light Load (50 users, 5/s spawn rate)
##############################################################################
echo -e "${YELLOW}[TEST 1/4] Running light load test (50 users)...${NC}"
locust -f locustfile-microservices.py \
    --host="$HOST" \
    --users 50 \
    --spawn-rate 5 \
    --run-time 60s \
    --headless \
    --html "$RESULTS_DIR/report_50users_${TIMESTAMP}.html" \
    --csv "$RESULTS_DIR/stats_50users_${TIMESTAMP}" \
    --loglevel INFO

echo -e "${GREEN}✓ Light load test completed${NC}"
echo ""

##############################################################################
# TEST 2: Medium Load (100 users, 10/s spawn rate)
##############################################################################
echo -e "${YELLOW}[TEST 2/4] Running medium load test (100 users)...${NC}"
locust -f locustfile-microservices.py \
    --host="$HOST" \
    --users 100 \
    --spawn-rate 10 \
    --run-time 120s \
    --headless \
    --html "$RESULTS_DIR/report_100users_${TIMESTAMP}.html" \
    --csv "$RESULTS_DIR/stats_100users_${TIMESTAMP}" \
    --loglevel INFO

echo -e "${GREEN}✓ Medium load test completed${NC}"
echo ""

##############################################################################
# TEST 3: High Load (500 users, 50/s spawn rate)
##############################################################################
echo -e "${YELLOW}[TEST 3/4] Running high load test (500 users)...${NC}"
locust -f locustfile-microservices.py \
    --host="$HOST" \
    --users 500 \
    --spawn-rate 50 \
    --run-time 180s \
    --headless \
    --html "$RESULTS_DIR/report_500users_${TIMESTAMP}.html" \
    --csv "$RESULTS_DIR/stats_500users_${TIMESTAMP}" \
    --loglevel INFO

echo -e "${GREEN}✓ High load test completed${NC}"
echo ""

##############################################################################
# TEST 4: Stress Test (1000 users, 100/s spawn rate)
##############################################################################
echo -e "${YELLOW}[TEST 4/4] Running stress test (1000 users)...${NC}"
locust -f locustfile-microservices.py \
    --host="$HOST" \
    --users 1000 \
    --spawn-rate 100 \
    --run-time 300s \
    --headless \
    --html "$RESULTS_DIR/report_1000users_${TIMESTAMP}.html" \
    --csv "$RESULTS_DIR/stats_1000users_${TIMESTAMP}" \
    --loglevel INFO

echo -e "${GREEN}✓ Stress test completed${NC}"
echo ""

##############################################################################
# Generate Summary Report
##############################################################################
echo -e "${YELLOW}Generating summary report...${NC}"

cat > "$RESULTS_DIR/summary_${TIMESTAMP}.txt" << EOF
================================================================================
MICROSERVICES PERFORMANCE TEST SUMMARY
================================================================================
Timestamp: ${TIMESTAMP}
Target Host: ${HOST}

TEST SCENARIOS:
1. Light Load:  50 users,   5/s spawn rate,  60s duration
2. Medium Load: 100 users, 10/s spawn rate, 120s duration
3. High Load:   500 users, 50/s spawn rate, 180s duration
4. Stress Test: 1000 users, 100/s spawn rate, 300s duration

RESULTS LOCATION:
- HTML Reports: ${RESULTS_DIR}/report_*users_${TIMESTAMP}.html
- CSV Stats:    ${RESULTS_DIR}/stats_*users_${TIMESTAMP}_stats.csv

METRICS COLLECTED:
- Throughput (Requests Per Second)
- Latency (p50, p95, p99)
- Error Rate (%)
- Response Time Distribution

ANALYSIS:
- Compare latency across different load levels
- Identify bottlenecks (which services slow down first)
- Measure error rate under stress
- Validate horizontal scaling effectiveness

NEXT STEPS:
1. Open HTML reports in browser
2. Analyze CSV files for detailed metrics
3. Compare with baseline/monolith if available
4. Identify optimization opportunities

================================================================================
EOF

echo -e "${GREEN}✓ Summary report generated${NC}"
echo ""

##############################################################################
# Display Results
##############################################################################
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}ALL TESTS COMPLETED SUCCESSFULLY!${NC}"
echo -e "${GREEN}======================================${NC}"
echo ""
echo -e "Results saved to: ${YELLOW}${RESULTS_DIR}/${NC}"
echo ""
echo -e "View reports:"
echo -e "  50 users:  ${RESULTS_DIR}/report_50users_${TIMESTAMP}.html"
echo -e "  100 users: ${RESULTS_DIR}/report_100users_${TIMESTAMP}.html"
echo -e "  500 users: ${RESULTS_DIR}/report_500users_${TIMESTAMP}.html"
echo -e "  1000 users: ${RESULTS_DIR}/report_1000users_${TIMESTAMP}.html"
echo ""
echo -e "Summary: ${RESULTS_DIR}/summary_${TIMESTAMP}.txt"
echo ""
echo -e "${YELLOW}To view HTML reports, open them in your browser:${NC}"
echo -e "  open ${RESULTS_DIR}/report_100users_${TIMESTAMP}.html"
echo ""
