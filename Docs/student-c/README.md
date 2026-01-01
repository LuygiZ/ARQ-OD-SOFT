# Student C - Lending & Review Functionality

## User Story

**As a reader, upon returning a Book, I want to leave a text comment about the Book and grading it (0-10).**

## Architecture Overview

### Services Architecture

```
                    +------------------+
                    |  Reader Service  |
                    |   (Port 8087)    |
                    |  Auth Provider   |
                    +--------+---------+
                             |
                             | JWT Token
                             v
+------------------+  +------------------+  +------------------+
|  Lending Service |  |   Book Service   |  | Other Services   |
|   (Port 8086)    |  |   (Port 8083)    |  | (Genre, Author)  |
|   Write Model    |  |   Read Model     |  |                  |
+--------+---------+  +--------+---------+  +------------------+
         |                     ^
         |   LendingReturned   |
         |   Event (RabbitMQ)  |
         +---------------------+
```

### CQRS Pattern

Following professor feedback, the architecture implements CQRS:

- **Command Side (Lending Service)**: Handles lending operations and generates `LendingReturnedEvent`
- **Query Side (Book Service)**: Stores denormalized `BookReview` entities for fast queries

This design ensures:
1. Reviews are conceptually tied to lendings (command side)
2. Reviews are queryable when viewing books (query side)
3. Separation of concerns between write and read models

## Services

### 1. Reader Service (Port 8087)

**Purpose**: Authentication provider and reader management

**Key Features**:
- JWT token issuance (RS256)
- User registration (readers, librarians)
- Role-based access control

**Endpoints**:
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/public/login | Login and get JWT | None |
| POST | /api/public/register | Register new reader | None |
| GET | /api/readers/{number} | Get reader by number | READER |
| GET | /api/readers/me | Get current reader | READER |

### 2. Lending Service (Port 8086)

**Purpose**: Manage book lendings and returns

**Key Features**:
- Create new lendings
- Return books with optional review (comment + rating 0-10)
- Outbox Pattern for reliable event publishing
- Optimistic locking

**Endpoints**:
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/lendings | Create lending | LIBRARIAN |
| POST | /api/v1/lendings/{year}/{seq}/return | Return with review | READER |
| GET | /api/v1/lendings/{year}/{seq} | Get lending | READER/LIBRARIAN |
| GET | /api/v1/lendings/reader/{number} | Get reader's lendings | READER/LIBRARIAN |

### 3. Book Service (Port 8083)

**Purpose**: Book queries including reviews

**Key Features**:
- Book queries (by ISBN, title, genre, author)
- Review queries for books
- Rating statistics (average, count)
- Event-driven review synchronization

**Endpoints**:
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/books/{isbn} | Get book by ISBN | Public |
| GET | /api/books/{isbn}/reviews | Get book reviews | Public |
| GET | /api/books/{isbn}/reviews/all | Get all reviews | Public |
| GET | /api/books/search/title | Search by title | Public |

## Domain Events

### LendingReturnedEvent

Published when a book is returned with a review:

```json
{
  "@type": "LendingReturned",
  "lendingNumber": "2025/1",
  "bookId": "978-0-13-468599-1",
  "readerId": 1,
  "readerNumber": "2025/100",
  "returnDate": "2025-01-15",
  "comment": "Great book, highly recommended!",
  "rating": 8,
  "daysOverdue": 0,
  "fineAmount": 0
}
```

## Data Flow

```
1. Reader logs in via Reader Service
   +------------------+
   |   POST /login    |
   +--------+---------+
            |
            v
   +------------------+
   | JWT Token issued |
   +--------+---------+

2. Reader returns book via Lending Service
   +---------------------------+
   | POST /lendings/2025/1/return |
   | { "comment": "...", "rating": 8 } |
   +------------+--------------+
                |
                v
   +---------------------------+
   | LendingEntity updated     |
   | Outbox event created      |
   +------------+--------------+
                |
                v
   +---------------------------+
   | Outbox Publisher sends    |
   | LendingReturnedEvent      |
   +------------+--------------+

3. Book Service consumes event
   +---------------------------+
   | RabbitMQ Consumer         |
   | receives event            |
   +------------+--------------+
                |
                v
   +---------------------------+
   | BookReview created        |
   | Rating stats updated      |
   +---------------------------+

4. Reviews available on Book queries
   +---------------------------+
   | GET /books/{isbn}/reviews |
   +---------------------------+
```

## Security

### JWT Authentication

- **Algorithm**: RS256 (RSA + SHA-256)
- **Issuer**: reader-service
- **Token Lifetime**: 1 hour
- **Public Key**: Shared across services via `/resources/certs/public.pem`

### Role-Based Access

| Role | Permissions |
|------|-------------|
| READER | View own lendings, return books, view books |
| LIBRARIAN | Create lendings, view all lendings |
| ADMIN | All operations |

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test -pl lending-service
mvn test -pl book-service
mvn test -pl reader-service
```

### CDC Tests (Pact)

Consumer-Driven Contract tests ensure services communicate correctly:

```bash
# Consumer test (Book Service)
mvn test -pl book-service -Dtest=*Pact*

# Provider test (Lending Service)
mvn test -pl lending-service -Dtest=*Pact*
```

### Mutation Tests (PIT)

```bash
# Run mutation tests
mvn org.pitest:pitest-maven:mutationCoverage -pl lending-service
mvn org.pitest:pitest-maven:mutationCoverage -pl book-service
mvn org.pitest:pitest-maven:mutationCoverage -pl reader-service
```

## Configuration

### RabbitMQ

- **Exchange**: `lms.events` (Topic)
- **Queues**:
  - `book-service.events` - Book service consumer
  - `lending-service.events` - Lending service consumer

### Routing Keys

| Event | Routing Key |
|-------|-------------|
| LendingCreated | lending.created |
| LendingReturned | lending.returned |
| BookRatingUpdated | review.book.rating_updated |

## Docker Deployment

```bash
# Build services
mvn clean package -DskipTests

# Build Docker images
docker build -t lending-service:latest ./lending-service
docker build -t book-service:latest ./book-service
docker build -t reader-service:latest ./reader-service

# Start infrastructure
cd infrastructure
docker-compose up -d
```

### Service Ports

| Service | Port |
|---------|------|
| PostgreSQL | 5432 |
| MongoDB | 27017 |
| Redis | 6379 |
| RabbitMQ | 5672 (AMQP), 15672 (Management) |
| Genre Service | 8081 |
| Author Service | 8082 |
| Book Service | 8083 |
| Saga Orchestrator | 8084 |
| Lending Service | 8086 |
| Reader Service | 8087 |

## Evaluation Criteria Met

1. **ADD (Attribute-Driven Design)**: Architecture decisions documented
2. **CQRS**: Lending (command) vs Book Reviews (query) separation
3. **CDC Tests**: Pact tests for LendingReturnedEvent contract
4. **Mutation Tests**: PIT configured for all services
5. **Outbox Pattern**: Reliable event publishing in Lending Service
6. **Event-Driven**: RabbitMQ integration for service communication
7. **Database per Service**: PostgreSQL databases per service
8. **Authentication**: JWT-based with RSA keys

## API Examples

### Login

```bash
curl -X POST http://localhost:8087/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"reader@test.com","password":"password123"}'
```

### Return Book with Review

```bash
curl -X POST http://localhost:8086/api/v1/lendings/2025/1/return \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"comment":"Great book!","rating":8}'
```

### Get Book Reviews

```bash
curl http://localhost:8083/api/books/978-0-13-468599-1/reviews
```
