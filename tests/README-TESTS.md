# Comprehensive Testing Suite - Microservices Architecture

This document provides complete instructions for running all tests that validate the microservices implementation against the evaluation criteria.

---

## Table of Contents

1. [Overview](#overview)
2. [Test Coverage Map](#test-coverage-map)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [Consumer-Driven Contract Tests (Pact)](#consumer-driven-contract-tests-pact)
6. [Integration Tests (Pattern Validation)](#integration-tests-pattern-validation)
7. [Performance/Load Tests (Locust)](#performanceload-tests-locust)
8. [Expected Results & Evidence](#expected-results--evidence)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This testing suite validates the microservices architecture implementation across multiple dimensions:

- **Consumer-Driven Contracts (CDC)**: Validates service interactions
- **Integration Tests**: Validates architectural patterns (Saga, CQRS, Outbox, etc.)
- **Performance Tests**: Validates scalability and performance characteristics

All tests are designed to provide **clear evidence** for the evaluation criteria.

---

## Test Coverage Map

| Criterion | Description | Test Type | Test Files |
|-----------|-------------|-----------|------------|
| **2.3** | Event-driven interactions via CDC | Pact CDC Tests | `GenreEventProviderTest.java`<br>`SagaServiceConsumerTest.java` |
| **2.4** | Performance/load testing | Locust | `locustfile-microservices.py`<br>`run-performance-tests.sh` |
| **2.1** | Implementation reflecting design decisions | Integration Tests | `SagaPatternIntegrationTest.java` |
| **2.2** | Implementation of functional requirements | Integration Tests | `SagaPatternIntegrationTest.java` |

---

## Prerequisites

### Software Requirements

1. **Java 21** - For running Spring Boot tests
   ```bash
   java -version  # Should show 21.x
   ```

2. **Maven 3.9+** - For building and running tests
   ```bash
   mvn -version
   ```

3. **Docker Desktop** - For Testcontainers
   ```bash
   docker --version
   docker ps  # Should connect successfully
   ```

4. **Python 3.11+** - For Locust performance tests
   ```bash
   python --version
   ```

5. **Locust** - Install via pip
   ```bash
   pip install -r tests/performance/locust/requirements.txt
   ```

### Services Running

Ensure all microservices are running:

```bash
# Check Genre Service (port 8081)
curl http://localhost:8081/api/genres

# Check Author Service (port 8082)
curl http://localhost:8082/api/authors

# Check Book Service (port 8083)
curl http://localhost:8083/api/books

# Check Saga Orchestrator (port 8084)
curl http://localhost:8084/actuator/health
```

If services are not running, start them:

```bash
# From project root
cd genre-service && mvn spring-boot:run &
cd ../author-service && mvn spring-boot:run &
cd ../book-service && mvn spring-boot:run &
cd ../saga-orchestrator && mvn spring-boot:run &
```

---

## Quick Start

Run all tests with a single command:

```bash
# From project root
./run-all-tests.sh
```

This will execute:
1. CDC Tests (Genre Service + Saga Orchestrator)
2. Integration Tests (Saga Pattern)
3. Performance Tests (100 users, 120s)

---

## Consumer-Driven Contract Tests (Pact)

### What They Test

CDC tests validate that:
- **Providers** (Genre, Author, Book services) produce events/responses in the format expected by consumers
- **Consumers** (Saga Orchestrator) can correctly consume these events/responses
- Contracts between services are maintained across changes

### Test 1: Genre Service Provider Test

**Location**: `genre-service/src/test/java/pt/psoft/genre/cdc/GenreEventProviderTest.java`

**What it validates**:
- Genre Service produces `GenreCreatedEvent` correctly
- Event schema matches consumer expectations
- Outbox Pattern persists events before publishing

**How to run**:

```bash
cd genre-service
mvn test -Dtest=GenreEventProviderTest
```

**Expected output**:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.psoft.genre.cdc.GenreEventProviderTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

**Evidence to collect**:
- Screenshot of test output
- Pact file generated: `genre-service/target/pacts/SagaOrchestrator-GenreService.json`

### Test 2: Saga Orchestrator Consumer Test

**Location**: `saga-orchestrator/src/test/java/pt/psoft/saga/cdc/SagaServiceConsumerTest.java`

**What it validates**:
- Saga Orchestrator expects correct contract from Genre Service
- Saga Orchestrator expects correct contract from Author Service
- Saga Orchestrator expects correct contract from Book Service
- Consumer contracts are enforceable

**How to run**:

```bash
cd saga-orchestrator
mvn test -Dtest=SagaServiceConsumerTest
```

**Expected output**:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

**Evidence to collect**:
- Screenshot of test output
- Pact files generated in: `saga-orchestrator/target/pacts/`

### Interpreting CDC Test Results

**SUCCESS Indicators**:
- ✅ All provider states are satisfied
- ✅ Request/response schemas match
- ✅ Pact files are generated
- ✅ No verification errors

**FAILURE Indicators**:
- ❌ Schema mismatch (e.g., missing field)
- ❌ Provider state setup fails
- ❌ Contract verification fails

---

## Integration Tests (Pattern Validation)

### Test: Saga Pattern Integration Test

**Location**: `saga-orchestrator/src/test/java/pt/psoft/saga/integration/SagaPatternIntegrationTest.java`

**What it validates**:
- ✅ **Saga Pattern**: Distributed transaction coordination
- ✅ **Compensation Logic**: Rollback on failure
- ✅ **Database-per-Service**: Saga uses Redis, services use PostgreSQL
- ✅ **Eventual Consistency**: Saga state transitions
- ✅ **FR-1**: Create Book atomically with Genre and Author

**Test Scenarios**:

1. **Happy Path** (Test 1)
   - Genre created → Author created → Book created
   - Saga state: COMPLETED
   - All steps recorded

2. **Rollback on Book Failure** (Test 2)
   - Genre created → Author created → Book FAILS
   - Compensation: Delete Author → Delete Genre
   - Saga state: COMPENSATED

3. **Partial Rollback** (Test 3)
   - Genre created → Author FAILS
   - Compensation: Delete Genre only
   - Book never attempted

4. **Idempotency** (Test 4)
   - Existing Genre reused
   - Only Author and Book created

**How to run**:

```bash
cd saga-orchestrator
mvn test -Dtest=SagaPatternIntegrationTest
```

**Expected output**:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.psoft.saga.integration.SagaPatternIntegrationTest
✅ TEST 1 PASSED: Happy Path - Saga completed successfully
✅ TEST 2 PASSED: Rollback - Compensation executed successfully
✅ TEST 3 PASSED: Partial Rollback - Only Genre compensated
✅ TEST 4 PASSED: Idempotency - Reused existing Genre
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

**Evidence to collect**:
- Screenshot of test output
- Testcontainers logs (Redis container)
- WireMock interaction logs

---

## Performance/Load Tests (Locust)

### What They Test

Performance tests validate:
- **Throughput**: Requests per second (RPS) under load
- **Latency**: Response time distribution (p50, p95, p99)
- **Error Rate**: % of failed requests
- **Scalability**: Performance with 50, 100, 500, 1000 concurrent users

### Test Scenarios

The Locust script simulates realistic user behavior:

**Write Operations (20% of traffic)**:
- Create Book via Saga (Genre + Author + Book)
- Create Genre directly
- Create Author directly

**Read Operations (80% of traffic)**:
- List all genres
- Search genre by name
- List all authors (CQRS read from MongoDB)
- List all books
- Get saga status (Redis query)

### How to Run

#### Option 1: Automated Test Suite (Recommended)

```bash
cd tests/performance/locust
chmod +x run-performance-tests.sh
./run-performance-tests.sh
```

This runs 4 test scenarios:
1. **Light Load**: 50 users, 60s
2. **Medium Load**: 100 users, 120s
3. **High Load**: 500 users, 180s
4. **Stress Test**: 1000 users, 300s

Results will be saved to `tests/performance/locust/results/`

#### Option 2: Manual Single Test

```bash
cd tests/performance/locust

# Example: 100 users, 10/s spawn rate, 120 seconds
locust -f locustfile-microservices.py \
    --host=http://localhost:8080 \
    --users 100 \
    --spawn-rate 10 \
    --run-time 120s \
    --headless \
    --html report_100users.html \
    --csv stats_100users
```

#### Option 3: Interactive Web UI

```bash
cd tests/performance/locust
locust -f locustfile-microservices.py --host=http://localhost:8080
```

Then open: http://localhost:8089

### Interpreting Performance Results

#### Key Metrics to Analyze

1. **Throughput (RPS)**
   - **Good**: >100 RPS with 100 users
   - **Excellent**: >500 RPS with 500 users

2. **Latency (p95)**
   - **Good**: <500ms for reads, <1000ms for writes
   - **Excellent**: <200ms for reads, <500ms for writes

3. **Error Rate**
   - **Acceptable**: <1% under normal load
   - **Good**: <5% under stress (1000 users)

4. **Saga Latency**
   - **Expected**: 500-1500ms (distributed transaction)
   - **Components**: Genre (100ms) + Author (150ms) + Book (200ms) + Coordination overhead

#### Sample Expected Results (100 users)

```
================================================================================
PERFORMANCE SUMMARY:
Total requests: 12,450
Total failures: 23
Failure rate: 0.18%
Average response time: 245.32 ms
Median response time: 180.00 ms
95th percentile: 520.00 ms
99th percentile: 890.00 ms
Requests per second: 103.75
================================================================================
```

#### What to Look For

**✅ SUCCESS Indicators**:
- Consistent throughput across load levels
- Latency stays under 1s for p95
- Error rate <1%
- System handles 500+ concurrent users

**⚠️ WARNING Indicators**:
- Latency increases exponentially with load
- Error rate >5%
- Throughput plateaus early

**❌ FAILURE Indicators**:
- Error rate >10%
- System crashes under load
- Response times >5s

### Evidence to Collect

From each test run, collect:

1. **HTML Report**: `report_100users.html`
   - Charts: Response Time, RPS, Number of Users
   - Tables: Request statistics, Failures

2. **CSV Stats**: `stats_100users_stats.csv`
   - Raw data for analysis
   - Import into Excel/Google Sheets

3. **Screenshots**:
   - Response time chart
   - RPS chart
   - Failure distribution

4. **Summary File**: `summary_<timestamp>.txt`
   - Test configuration
   - High-level results

---

## Expected Results & Evidence

### Evidence Checklist

For each criterion, collect the following evidence:

#### 2.3: Event-driven interactions via CDC

**Tests**: `GenreEventProviderTest`, `SagaServiceConsumerTest`

**Evidence**:
- [ ] Screenshot of CDC test passing
- [ ] Pact contract files (JSON)
- [ ] Console output showing contract verification
- [ ] Example: Event schema validation passed

#### 2.4: Performance/load testing

**Tests**: `locustfile-microservices.py`, `run-performance-tests.sh`

**Evidence**:
- [ ] HTML reports for 50, 100, 500, 1000 users
- [ ] CSV statistics files
- [ ] Screenshot of response time chart
- [ ] Screenshot of RPS chart
- [ ] Performance summary showing:
  - Throughput (RPS)
  - Latency (p50, p95, p99)
  - Error rate
  - Scalability trend

#### 2.1: Implementation reflecting design decisions

**Tests**: `SagaPatternIntegrationTest`

**Evidence**:
- [ ] Screenshot of all 4 tests passing
- [ ] Console logs showing:
  - Saga state transitions
  - Compensation logic execution
  - Testcontainers startup (Redis)
  - WireMock interactions
- [ ] Saga instance JSON from Redis

#### 2.2: Functional requirements

**Tests**: `SagaPatternIntegrationTest` (Happy Path)

**Evidence**:
- [ ] Screenshot of Test 1 (Happy Path) passing
- [ ] Saga response showing:
  - Genre created
  - Author created
  - Book created
  - Saga status: COMPLETED

---

## Example Evidence Report

Create a document: `TEST-EVIDENCE-REPORT.md`

```markdown
# Test Evidence Report

## 2.3: Consumer-Driven Contract Tests

### GenreEventProviderTest
- **Status**: ✅ PASSED
- **Date**: 2026-01-03
- **Evidence**:
  - All 4 provider states verified
  - Pact file: `SagaOrchestrator-GenreService.json`
  - Screenshot: `evidence/cdc-genre-provider.png`

### SagaServiceConsumerTest
- **Status**: ✅ PASSED
- **Date**: 2026-01-03
- **Evidence**:
  - All 4 consumer tests passed
  - Contracts generated for: Genre, Author, Book services
  - Screenshot: `evidence/cdc-saga-consumer.png`

## 2.4: Performance Testing

### Test Configuration
- Users: 100
- Duration: 120s
- Target: http://localhost:8080

### Results
- **Throughput**: 103.75 RPS
- **Latency (p95)**: 520ms
- **Error Rate**: 0.18%

### Evidence
- HTML Report: `results/report_100users.html`
- Screenshot: `evidence/performance-100users.png`

## 2.1: Saga Pattern Integration

### Test Scenarios
1. ✅ Happy Path - Completed
2. ✅ Rollback on Book Failure - Compensated
3. ✅ Partial Rollback - Compensated
4. ✅ Idempotency - Reused existing Genre

### Evidence
- All tests passed (4/4)
- Screenshot: `evidence/saga-integration-tests.png`
- Testcontainers logs: `evidence/testcontainers-redis.log`

## 2.2: Functional Requirements

### FR-1: Create Book Atomically
- **Status**: ✅ VALIDATED
- **Test**: SagaPatternIntegrationTest (Happy Path)
- **Evidence**: Saga completed with Genre + Author + Book created

```

---

## Troubleshooting

### Issue: Testcontainers fails to start

**Symptom**: "Could not find a valid Docker environment"

**Solution**:
```bash
# Ensure Docker Desktop is running
docker ps

# Check Docker daemon
docker info
```

### Issue: Pact tests fail with "No pact files found"

**Symptom**: "No pacts found in directory"

**Solution**:
```bash
# Ensure pact files exist
ls -la saga-orchestrator/src/test/resources/pacts/

# If missing, create directory
mkdir -p saga-orchestrator/src/test/resources/pacts/
```

### Issue: Locust fails to connect to services

**Symptom**: "Connection refused"

**Solution**:
```bash
# Check if services are running
curl http://localhost:8081/api/genres
curl http://localhost:8082/api/authors
curl http://localhost:8083/api/books
curl http://localhost:8084/actuator/health

# Start services if not running
cd <service-dir> && mvn spring-boot:run
```

### Issue: Integration tests timeout

**Symptom**: "Test timed out after 30000ms"

**Solution**:
```bash
# Increase timeout in test
@Test(timeout = 60000)  // 60 seconds

# Or check if WireMock ports are available
lsof -i :8081
lsof -i :8082
lsof -i :8083
```

### Issue: Performance tests show high error rate

**Symptom**: Error rate >10%

**Solution**:
1. Check service logs for errors
2. Reduce number of users
3. Increase service resources (RAM, CPU)
4. Check database connections

---

## Summary

This testing suite provides comprehensive validation of the microservices architecture:

1. **CDC Tests**: Validate service contracts are maintained
2. **Integration Tests**: Validate architectural patterns work correctly
3. **Performance Tests**: Validate system can handle load

All tests are designed to produce **clear, demonstrable evidence** for evaluation criteria.

**Next Steps**:
1. Run all tests: `./run-all-tests.sh`
2. Collect evidence: Screenshots, reports, logs
3. Create evidence report: `TEST-EVIDENCE-REPORT.md`
4. Analyze results and document findings

---

**Questions?**

- Check troubleshooting section above
- Review test code comments (extensive documentation)
- Check Locust documentation: https://docs.locust.io/
- Check Pact documentation: https://docs.pact.io/

Good luck with your evaluation! 🚀
