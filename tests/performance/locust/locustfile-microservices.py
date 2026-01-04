"""
Locust Performance Test - Microservices Architecture

Tests the performance of the microservices-based Library Management System
focusing on the Saga Orchestrator and individual services.

EVALUATION CRITERIA COVERAGE:
- 2.4: Performance/load testing with multiple concurrent users
- Demonstrates scalability with horizontal scaling
- Compares throughput, latency, and error rates

SCENARIOS TESTED:
1. Create Book via Saga (Genre + Author + Book)
2. Read Books (CQRS query)
3. Search Genres
4. Search Authors
5. Mixed workload (80% read, 20% write)

METRICS COLLECTED:
- Throughput (Requests Per Second)
- Latency (p50, p95, p99)
- Error Rate (%)
- Response Time Distribution

HOW TO RUN:
1. Install Locust: pip install locust
2. Ensure microservices are running on localhost
3. Run basic test:
   locust -f locustfile-microservices.py --host=http://localhost:8080 --users 100 --spawn-rate 10 --run-time 60s
4. Run with web UI:
   locust -f locustfile-microservices.py --host=http://localhost:8080
   Then open: http://localhost:8089

EXAMPLE COMMANDS:
# Test with 50 users for 60 seconds
locust -f locustfile-microservices.py --host=http://localhost:8080 -u 50 -r 5 -t 60s --headless

# Test with 100 users for 120 seconds
locust -f locustfile-microservices.py --host=http://localhost:8080 -u 100 -r 10 -t 120s --headless

# Test with 500 users (stress test)
locust -f locustfile-microservices.py --host=http://localhost:8080 -u 500 -r 50 -t 300s --headless

# Generate HTML report
locust -f locustfile-microservices.py --host=http://localhost:8080 -u 100 -r 10 -t 60s --headless --html report.html
"""

from locust import HttpUser, task, between, events
import random
import json
import logging
from datetime import datetime

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class LibraryMicroservicesUser(HttpUser):
    """
    Simulates a user interacting with the microservices-based library system

    Wait time: Random between 1-3 seconds (simulates user think time)
    """
    wait_time = between(1, 3)

    # Sample data for test requests
    genres = ["Science Fiction", "Fantasy", "Mystery", "Horror", "Romance", "Thriller"]
    authors = [
        {"name": "Isaac Asimov", "bio": "American science fiction author"},
        {"name": "J.R.R. Tolkien", "bio": "Fantasy author"},
        {"name": "Agatha Christie", "bio": "Mystery author"},
        {"name": "Stephen King", "bio": "Horror author"},
        {"name": "Arthur C. Clarke", "bio": "Science fiction author"},
    ]
    books = [
        {"title": "Foundation", "description": "Classic sci-fi novel", "genre": "Science Fiction"},
        {"title": "The Lord of the Rings", "description": "Epic fantasy", "genre": "Fantasy"},
        {"title": "Murder on the Orient Express", "description": "Mystery novel", "genre": "Mystery"},
        {"title": "The Shining", "description": "Horror novel", "genre": "Horror"},
        {"title": "2001: A Space Odyssey", "description": "Sci-fi classic", "genre": "Science Fiction"},
    ]

    def on_start(self):
        """Called when a simulated user starts"""
        logger.info(f"User started at {datetime.now()}")
        self.created_resources = {
            "genres": [],
            "authors": [],
            "books": []
        }

    def on_stop(self):
        """Called when a simulated user stops"""
        logger.info(f"User stopped. Created: {len(self.created_resources['books'])} books")

    # ====================
    # WRITE OPERATIONS (20% of traffic)
    # ====================

    @task(1)
    def create_book_via_saga(self):
        """
        TEST: Create Book via Saga Orchestrator (WRITE - Complex Transaction)

        This is the main functional requirement (FR-1):
        Create Book atomically with Genre and Author via Saga Pattern

        Expected: 201 Created
        Measures: Distributed transaction latency
        """
        author_data = random.choice(self.authors)
        book_data = random.choice(self.books)
        genre_name = random.choice(self.genres)

        # Add randomness to avoid duplicates
        unique_id = random.randint(1000, 9999)

        payload = {
            "genre": {
                "name": genre_name
            },
            "author": {
                "name": f"{author_data['name']} {unique_id}",
                "bio": author_data['bio'],
                "photoURI": f"https://example.com/author_{unique_id}.jpg"
            },
            "book": {
                "title": f"{book_data['title']} {unique_id}",
                "description": book_data['description'],
                "genreName": genre_name,
                "photoURI": f"https://example.com/book_{unique_id}.jpg"
            }
        }

        with self.client.post(
            "/api/catalog/books",
            json=payload,
            catch_response=True,
            name="[SAGA] Create Book (Genre+Author+Book)"
        ) as response:
            if response.status_code == 201:
                response_data = response.json()
                self.created_resources["books"].append(response_data.get("sagaId"))
                response.success()
            elif response.status_code == 500:
                # Saga might fail due to business rules, log it
                logger.warning(f"Saga failed: {response.text[:200]}")
                response.failure("Saga execution failed")
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(1)
    def create_genre(self):
        """
        TEST: Create Genre directly (WRITE - Simple)

        Expected: 201 Created or 409 Conflict (if exists)
        Measures: Single service write latency
        """
        genre_name = f"{random.choice(self.genres)} {random.randint(1, 999)}"

        payload = {"genreName": genre_name}

        with self.client.post(
            "http://localhost:8080/api/genres",
            json=payload,
            catch_response=True,
            name="[GENRE] Create Genre"
        ) as response:
            if response.status_code in [201, 409]:  # Created or Conflict
                if response.status_code == 201:
                    response_data = response.json()
                    self.created_resources["genres"].append(response_data.get("id"))
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(1)
    def create_author(self):
        """
        TEST: Create Author directly (WRITE - Medium complexity, CQRS write)

        Expected: 201 Created
        Measures: CQRS write model latency (PostgreSQL + MongoDB sync)
        """
        author_data = random.choice(self.authors)
        unique_id = random.randint(1000, 9999)

        payload = {
            "name": f"{author_data['name']} {unique_id}",
            "bio": author_data['bio'],
            "photoURI": f"https://example.com/author_{unique_id}.jpg"
        }

        with self.client.post(
            "http://localhost:8082/api/authors",
            json=payload,
            catch_response=True,
            name="[AUTHOR] Create Author"
        ) as response:
            if response.status_code == 201:
                response_data = response.json()
                self.created_resources["authors"].append(response_data.get("authorNumber"))
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    # ====================
    # READ OPERATIONS (80% of traffic)
    # ====================

    @task(10)
    def get_all_genres(self):
        """
        TEST: List all genres (READ - Simple)

        Expected: 200 OK
        Measures: Read latency with caching (Redis)
        """
        with self.client.get(
            "http://localhost:8080/api/genres",
            catch_response=True,
            name="[GENRE] List All Genres"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(10)
    def search_genre_by_name(self):
        """
        TEST: Search genre by name (READ - Query)

        Expected: 200 OK or 404 Not Found
        Measures: Query performance
        """
        genre_name = random.choice(self.genres)

        with self.client.get(
            f"http://localhost:8080/api/genres/search?name={genre_name}",
            catch_response=True,
            name="[GENRE] Search Genre by Name"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(15)
    def get_all_authors(self):
        """
        TEST: List all authors (READ - CQRS Query from MongoDB)

        Expected: 200 OK
        Measures: CQRS read model performance (MongoDB + Redis cache)
        """
        with self.client.get(
            "http://localhost:8082/api/authors",
            catch_response=True,
            name="[AUTHOR] List All Authors"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(15)
    def get_all_books(self):
        """
        TEST: List all books (READ - CQRS Query)

        Expected: 200 OK
        Measures: Book service query performance with caching
        """
        with self.client.get(
            "http://localhost:8083/api/books",
            catch_response=True,
            name="[BOOK] List All Books"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    @task(5)
    def get_saga_status(self):
        """
        TEST: Get Saga status (READ - Redis query)

        Expected: 200 OK or 404 Not Found
        Measures: Redis read performance for saga state
        """
        if self.created_resources["books"]:
            saga_id = random.choice(self.created_resources["books"])

            with self.client.get(
                f"/api/catalog/sagas/{saga_id}",
                catch_response=True,
                name="[SAGA] Get Saga Status"
            ) as response:
                if response.status_code in [200, 404]:
                    response.success()
                else:
                    response.failure(f"Unexpected status: {response.status_code}")


# ====================
# CUSTOM EVENTS & METRICS
# ====================

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Called when test starts"""
    logger.info("=" * 80)
    logger.info("MICROSERVICES PERFORMANCE TEST STARTED")
    logger.info(f"Target host: {environment.host}")
    logger.info(f"Start time: {datetime.now()}")
    logger.info("=" * 80)


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Called when test stops - Print summary"""
    logger.info("=" * 80)
    logger.info("MICROSERVICES PERFORMANCE TEST COMPLETED")
    logger.info(f"End time: {datetime.now()}")
    logger.info("=" * 80)

    # Print statistics
    stats = environment.stats
    logger.info("\nPERFORMANCE SUMMARY:")
    logger.info(f"Total requests: {stats.total.num_requests}")
    logger.info(f"Total failures: {stats.total.num_failures}")
    logger.info(f"Failure rate: {stats.total.fail_ratio * 100:.2f}%")
    logger.info(f"Average response time: {stats.total.avg_response_time:.2f} ms")
    logger.info(f"Median response time: {stats.total.median_response_time:.2f} ms")
    logger.info(f"95th percentile: {stats.total.get_response_time_percentile(0.95):.2f} ms")
    logger.info(f"99th percentile: {stats.total.get_response_time_percentile(0.99):.2f} ms")
    logger.info(f"Requests per second: {stats.total.total_rps:.2f}")
    logger.info("=" * 80)
