#!/bin/bash

##############################################################################
# Master Test Execution Script
#
# Runs ALL tests in sequence:
# 1. Consumer-Driven Contract Tests (Pact)
# 2. Integration Tests (Saga Pattern)
# 3. Performance Tests (Locust)
#
# EVALUATION CRITERIA COVERAGE: 2.1, 2.2, 2.3, 2.4
#
# HOW TO USE:
# chmod +x run-all-tests.sh
# ./run-all-tests.sh
##############################################################################

set -e  # Exit on error

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_DIR="test-results-${TIMESTAMP}"
mkdir -p "$LOG_DIR"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         COMPREHENSIVE TESTING SUITE - EXECUTION           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Timestamp: ${TIMESTAMP}${NC}"
echo -e "${YELLOW}Results Directory: ${LOG_DIR}${NC}"
echo ""

##############################################################################
# PHASE 1: Consumer-Driven Contract Tests (Pact)
##############################################################################
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE} PHASE 1: Consumer-Driven Contract Tests (Pact CDC)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Test 1.1: Genre Service Provider Test
echo -e "${YELLOW}[1.1] Running Genre Service Provider Test...${NC}"
cd genre-service
mvn test -Dtest=GenreEventProviderTest > "../${LOG_DIR}/cdc-genre-provider.log" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Genre Provider Test PASSED${NC}"
else
    echo -e "${RED}✗ Genre Provider Test FAILED - Check ${LOG_DIR}/cdc-genre-provider.log${NC}"
    exit 1
fi

cd ..
echo ""

# Test 1.2: Saga Orchestrator Consumer Test
echo -e "${YELLOW}[1.2] Running Saga Orchestrator Consumer Test...${NC}"
cd saga-orchestrator
mvn test -Dtest=SagaServiceConsumerTest > "../${LOG_DIR}/cdc-saga-consumer.log" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Saga Consumer Test PASSED${NC}"
else
    echo -e "${RED}✗ Saga Consumer Test FAILED - Check ${LOG_DIR}/cdc-saga-consumer.log${NC}"
    exit 1
fi

cd ..
echo ""

echo -e "${GREEN}✓ PHASE 1 COMPLETED - All CDC tests passed${NC}"
echo ""

##############################################################################
# PHASE 2: Integration Tests (Pattern Validation)
##############################################################################
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE} PHASE 2: Integration Tests (Saga Pattern Validation)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

echo -e "${YELLOW}[2.1] Running Saga Pattern Integration Tests...${NC}"
echo -e "  - Happy Path: Create Book + Author + Genre"
echo -e "  - Rollback: Book creation fails, compensation executed"
echo -e "  - Partial Rollback: Author fails, only Genre compensated"
echo -e "  - Idempotency: Reuse existing Genre"
echo ""

cd saga-orchestrator
mvn test -Dtest=SagaPatternIntegrationTest > "../${LOG_DIR}/integration-saga.log" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Saga Pattern Integration Tests PASSED (4/4)${NC}"
else
    echo -e "${RED}✗ Integration Tests FAILED - Check ${LOG_DIR}/integration-saga.log${NC}"
    exit 1
fi

cd ..
echo ""

echo -e "${GREEN}✓ PHASE 2 COMPLETED - All integration tests passed${NC}"
echo ""

##############################################################################
# PHASE 3: Performance/Load Tests (Locust)
##############################################################################
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE} PHASE 3: Performance/Load Tests (Locust)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Check if Locust is installed
if ! command -v locust &> /dev/null; then
    echo -e "${RED}ERROR: Locust is not installed${NC}"
    echo "Install with: pip install -r tests/performance/locust/requirements.txt"
    exit 1
fi

# Check if services are running
echo -e "${YELLOW}[3.1] Checking if microservices are running...${NC}"

SERVICES_OK=true

if ! curl -s -o /dev/null -w "%{http_code}" "http://localhost:8081/api/genres" > /dev/null 2>&1; then
    echo -e "${RED}✗ Genre Service (port 8081) is not responding${NC}"
    SERVICES_OK=false
fi

if ! curl -s -o /dev/null -w "%{http_code}" "http://localhost:8082/api/authors" > /dev/null 2>&1; then
    echo -e "${RED}✗ Author Service (port 8082) is not responding${NC}"
    SERVICES_OK=false
fi

if ! curl -s -o /dev/null -w "%{http_code}" "http://localhost:8083/api/books" > /dev/null 2>&1; then
    echo -e "${RED}✗ Book Service (port 8083) is not responding${NC}"
    SERVICES_OK=false
fi

if [ "$SERVICES_OK" = false ]; then
    echo -e "${RED}ERROR: Not all services are running. Please start them before running performance tests.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All microservices are running${NC}"
echo ""

# Run performance test (100 users, 120s)
echo -e "${YELLOW}[3.2] Running performance test (100 users, 120 seconds)...${NC}"

cd tests/performance/locust

locust -f locustfile-microservices.py \
    --host=http://localhost:8080 \
    --users 100 \
    --spawn-rate 10 \
    --run-time 120s \
    --headless \
    --html "../../../${LOG_DIR}/performance-report.html" \
    --csv "../../../${LOG_DIR}/performance-stats" \
    --loglevel INFO > "../../../${LOG_DIR}/performance-locust.log" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Performance Test PASSED${NC}"
else
    echo -e "${RED}✗ Performance Test FAILED - Check ${LOG_DIR}/performance-locust.log${NC}"
    exit 1
fi

cd ../../..
echo ""

echo -e "${GREEN}✓ PHASE 3 COMPLETED - Performance tests passed${NC}"
echo ""

##############################################################################
# Generate Summary Report
##############################################################################
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE} GENERATING SUMMARY REPORT${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

cat > "${LOG_DIR}/SUMMARY.md" << EOF
# Test Execution Summary

**Date**: ${TIMESTAMP}

---

## Test Results Overview

| Phase | Test | Status | Evidence |
|-------|------|--------|----------|
| 1.1 | Genre Service Provider Test (CDC) | ✅ PASSED | cdc-genre-provider.log |
| 1.2 | Saga Consumer Test (CDC) | ✅ PASSED | cdc-saga-consumer.log |
| 2.1 | Saga Pattern Integration Tests | ✅ PASSED | integration-saga.log |
| 3.1 | Performance Test (100 users) | ✅ PASSED | performance-report.html |

---

## Evaluation Criteria Coverage

### 2.3: Event-driven interactions via CDC
- ✅ GenreEventProviderTest: Validates Genre Service publishes events correctly
- ✅ SagaServiceConsumerTest: Validates Saga consumes events from all services
- **Evidence**: cdc-genre-provider.log, cdc-saga-consumer.log

### 2.4: Performance/load testing
- ✅ Performance test with 100 concurrent users for 120 seconds
- **Metrics**: See performance-report.html for:
  - Throughput (RPS)
  - Latency (p50, p95, p99)
  - Error rate
- **Evidence**: performance-report.html, performance-stats_stats.csv

### 2.1: Implementation reflecting design decisions
- ✅ Saga Pattern: Orchestration of distributed transactions
- ✅ Compensation: Rollback logic validated
- ✅ Database-per-Service: Saga uses Redis, services use PostgreSQL
- **Evidence**: integration-saga.log

### 2.2: Implementation of functional requirements
- ✅ FR-1: Create Book atomically with Genre and Author
- **Evidence**: integration-saga.log (Happy Path test)

---

## Files Generated

- \`cdc-genre-provider.log\` - Genre Service Provider Test output
- \`cdc-saga-consumer.log\` - Saga Consumer Test output
- \`integration-saga.log\` - Saga Integration Test output
- \`performance-report.html\` - Performance test HTML report
- \`performance-stats_stats.csv\` - Performance test CSV data
- \`performance-locust.log\` - Locust execution log

---

## Next Steps

1. **Review HTML Performance Report**:
   \`\`\`bash
   open ${LOG_DIR}/performance-report.html
   \`\`\`

2. **Extract Key Metrics**:
   - Check CSV file: \`performance-stats_stats.csv\`
   - Note: Throughput, Latency (p95), Error Rate

3. **Create Evidence Document**:
   - Take screenshots of:
     - CDC test output
     - Integration test output (4/4 passed)
     - Performance report charts
   - Document in TEST-EVIDENCE-REPORT.md

4. **Analysis**:
   - Compare performance with expected baselines
   - Identify any bottlenecks
   - Document findings

---

**All tests completed successfully!** ✅

For detailed instructions, see: \`tests/README-TESTS.md\`
EOF

echo -e "${GREEN}✓ Summary report generated: ${LOG_DIR}/SUMMARY.md${NC}"
echo ""

##############################################################################
# Final Summary
##############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║              🎉 ALL TESTS COMPLETED SUCCESSFULLY 🎉        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}RESULTS:${NC}"
echo -e "  ✅ CDC Tests: 2/2 passed"
echo -e "  ✅ Integration Tests: 4/4 passed"
echo -e "  ✅ Performance Tests: 1/1 passed"
echo ""

echo -e "${YELLOW}EVIDENCE COLLECTED:${NC}"
echo -e "  📁 Location: ${LOG_DIR}/"
echo -e "  📄 Summary: ${LOG_DIR}/SUMMARY.md"
echo -e "  📊 Performance Report: ${LOG_DIR}/performance-report.html"
echo ""

echo -e "${YELLOW}NEXT STEPS:${NC}"
echo -e "  1. Review summary: cat ${LOG_DIR}/SUMMARY.md"
echo -e "  2. View performance report: open ${LOG_DIR}/performance-report.html"
echo -e "  3. Create evidence document from results"
echo ""

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""
