# Quick Reference Guide - Testing Commands

This is a quick cheat sheet for running individual tests. For detailed documentation, see [README-TESTS.md](README-TESTS.md).

---

## Run All Tests (One Command)

```bash
# From project root
./run-all-tests.sh
```

---

## Consumer-Driven Contract Tests (Pact)

### Genre Service Provider Test

```bash
cd genre-service
mvn test -Dtest=GenreEventProviderTest
```

**Output**: Validates Genre Service publishes events correctly
**Evidence**: `target/pacts/SagaOrchestrator-GenreService.json`

### Saga Orchestrator Consumer Test

```bash
cd saga-orchestrator
mvn test -Dtest=SagaServiceConsumerTest
```

**Output**: Validates Saga expects correct contracts from all services
**Evidence**: `target/pacts/*.json`

---

## Integration Tests

### Saga Pattern Integration Test

```bash
cd saga-orchestrator
mvn test -Dtest=SagaPatternIntegrationTest
```

**Output**: 4/4 tests (Happy Path, Rollback, Partial Rollback, Idempotency)
**Evidence**: Console output with ✅ markers

### Run with verbose output

```bash
mvn test -Dtest=SagaPatternIntegrationTest -X
```

---

## Performance Tests

### Quick Test (100 users, 60 seconds)

```bash
cd tests/performance/locust

locust -f locustfile-microservices.py \
    --host=http://localhost:8080 \
    --users 100 \
    --spawn-rate 10 \
    --run-time 60s \
    --headless \
    --html report.html
```

### Full Test Suite (All load levels)

```bash
cd tests/performance/locust
chmod +x run-performance-tests.sh
./run-performance-tests.sh
```

**Output**: 4 HTML reports (50, 100, 500, 1000 users)
**Evidence**: `results/` directory

### Interactive Web UI

```bash
cd tests/performance/locust
locust -f locustfile-microservices.py --host=http://localhost:8080
```

Open: http://localhost:8089

---

## Common Commands

### Check if services are running

```bash
# Genre Service (8081)
curl http://localhost:8081/api/genres

# Author Service (8082)
curl http://localhost:8082/api/authors

# Book Service (8083)
curl http://localhost:8083/api/books

# Saga Orchestrator (8084)
curl http://localhost:8084/actuator/health
```

### Start all services

```bash
# Terminal 1 - Genre Service
cd genre-service && mvn spring-boot:run

# Terminal 2 - Author Service
cd author-service && mvn spring-boot:run

# Terminal 3 - Book Service
cd book-service && mvn spring-boot:run

# Terminal 4 - Saga Orchestrator
cd saga-orchestrator && mvn spring-boot:run
```

### Check Docker (for Testcontainers)

```bash
docker ps
docker info
```

### Install Python dependencies (for Locust)

```bash
pip install -r tests/performance/locust/requirements.txt
```

---

## Troubleshooting

### Pact tests fail: "No pacts found"

```bash
# Create directory
mkdir -p genre-service/src/test/resources/pacts/

# Copy pact files if needed
cp pacts/*.json genre-service/src/test/resources/pacts/
```

### Testcontainers fails: "Docker not available"

```bash
# Start Docker Desktop
# Then verify
docker ps
```

### Locust fails: "Connection refused"

```bash
# Ensure all services are running
curl http://localhost:8081/api/genres
curl http://localhost:8082/api/authors
curl http://localhost:8083/api/books
```

### Integration test timeout

```bash
# Check if WireMock ports are available
lsof -i :8081  # Should be free or used by actual service
lsof -i :8082
lsof -i :8083
```

---

## Viewing Results

### Open HTML Performance Report

```bash
# Mac
open tests/performance/locust/results/report_100users.html

# Windows
start tests/performance/locust/results/report_100users.html

# Linux
xdg-open tests/performance/locust/results/report_100users.html
```

### View CSV Stats in Excel

```bash
# Open file
open tests/performance/locust/results/stats_100users_stats.csv
```

### View Pact Contracts

```bash
cat genre-service/target/pacts/SagaOrchestrator-GenreService.json | jq '.'
```

---

## Test Execution Checklist

Before running tests:

- [ ] Docker Desktop is running
- [ ] All microservices are running (ports 8081-8084)
- [ ] PostgreSQL is running (port 5432)
- [ ] Redis is running (port 6379)
- [ ] RabbitMQ is running (port 5672)
- [ ] Python + Locust installed (for performance tests)
- [ ] Maven + Java 21 installed

---

## Expected Results Summary

| Test | Duration | Expected Result |
|------|----------|----------------|
| GenreEventProviderTest | ~10s | 4/4 tests pass, pact file generated |
| SagaServiceConsumerTest | ~8s | 4/4 tests pass, 3 pact files generated |
| SagaPatternIntegrationTest | ~15s | 4/4 tests pass, Testcontainers start |
| Performance Test (100 users) | 120s | RPS >50, Error rate <1%, p95 <1000ms |
| **Total (run-all-tests.sh)** | ~5min | All tests pass ✅ |

---

## Evidence Collection

After running tests, collect:

1. **Screenshots**:
   - CDC test output
   - Integration test output (4/4 passed)
   - Performance HTML report (charts)

2. **Files**:
   - Pact JSON contracts
   - Performance CSV stats
   - Test logs

3. **Metrics**:
   - Throughput (RPS)
   - Latency (p50, p95, p99)
   - Error rate (%)

4. **Documentation**:
   - Fill in TEST-EVIDENCE-REPORT.md template
   - Add screenshots and analysis

---

## Help & Documentation

- Full documentation: `tests/README-TESTS.md`
- Evidence template: `docs/TEST-EVIDENCE-REPORT.md`
- Locust docs: https://docs.locust.io/
- Pact docs: https://docs.pact.io/
- Testcontainers docs: https://testcontainers.com/

---

**Last Updated**: 2026-01-03
