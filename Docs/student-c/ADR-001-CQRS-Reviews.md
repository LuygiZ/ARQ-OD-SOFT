# ADR-001: CQRS for Reviews

## Status

Accepted

## Context

Student C functionality requires readers to leave comments and ratings (0-10) when returning books. The initial design had a separate Review Service, but professor feedback questioned this approach:

> "Does having a Review Service make sense? Reviews are conceptually connected to lendings. Lendings = Commands, Reviews = Queries (CQRS). Reviews should be in BookQuery so users can see reviews when viewing a book."

## Decision

We implement CQRS (Command Query Responsibility Segregation) pattern:

1. **Command Side (Lending Service)**
   - Handles the return operation with optional review
   - Stores review data in `LendingEntity` (rating, comment)
   - Publishes `LendingReturnedEvent` via Outbox Pattern

2. **Query Side (Book Service)**
   - Consumes `LendingReturnedEvent`
   - Stores denormalized `BookReview` entities
   - Provides review queries per book
   - Maintains aggregated rating statistics

## Consequences

### Positive

- **Separation of Concerns**: Write logic in Lending, read logic in Book
- **Query Performance**: Reviews are pre-aggregated for fast book queries
- **Eventual Consistency**: Acceptable for review display (not critical)
- **Scalability**: Book queries scale independently from lending writes

### Negative

- **Complexity**: Two models to maintain
- **Eventual Consistency**: Slight delay between return and review visibility
- **Data Duplication**: Review data exists in both services

### Neutral

- Removed separate Review Service (reduced service count)
- Event-driven synchronization via RabbitMQ

## Implementation Details

### Event Flow

```
LendingService                    BookService
     |                                 |
     | setReturned(comment, rating)    |
     |                                 |
     | publish(LendingReturnedEvent)   |
     |-------------------------------->|
     |                                 | handleLendingReturned()
     |                                 | create BookReview
     |                                 | updateRatingStats()
     |                                 |
```

### Data Models

**Command Model (Lending Service)**:
```java
@Entity
class LendingEntity {
    // ... lending fields
    String comment;      // Optional review comment
    Integer rating;      // Optional rating 0-10
    LocalDate returnedDate;
}
```

**Query Model (Book Service)**:
```java
@Entity
class BookReview {
    String lendingNumber;  // Idempotency key
    String isbn;
    String readerNumber;
    String comment;
    Integer rating;
    LocalDate returnDate;
}
```

## Alternatives Considered

1. **Separate Review Service**: Rejected - adds unnecessary complexity
2. **Reviews only in Lending**: Rejected - poor query performance for book views
3. **Reviews only in Book**: Rejected - violates domain boundaries (review is part of return action)

## References

- CQRS Pattern: https://martinfowler.com/bliki/CQRS.html
- Domain Events: https://martinfowler.com/eaaDev/DomainEvent.html
