# ADR-002: Outbox Pattern for Reliable Event Publishing

## Status

Accepted

## Context

In a microservices architecture, we need to:
1. Update the database (e.g., mark lending as returned)
2. Publish a domain event (e.g., `LendingReturnedEvent`)

These must happen atomically - if the database update succeeds but event publishing fails, the system becomes inconsistent.

## Decision

We implement the **Outbox Pattern**:

1. Within the same database transaction:
   - Update the main entity
   - Insert an event record into an `outbox_events` table

2. A separate process (Outbox Publisher) polls the outbox table and:
   - Publishes events to RabbitMQ
   - Marks events as published

## Consequences

### Positive

- **Atomicity**: Entity update and event creation are in the same transaction
- **Reliability**: No lost events due to messaging failures
- **Ordering**: Events can be processed in order using sequence numbers
- **Retry**: Failed publications can be retried

### Negative

- **Latency**: Small delay between commit and event publication
- **Complexity**: Additional table and publisher process
- **Polling**: Publisher must poll (or use CDC)

## Implementation Details

### Outbox Entity

```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;  // e.g., "LENDING"
    private String aggregateId;    // e.g., "2025/1"
    private String eventType;      // e.g., "RETURNED"

    @Column(columnDefinition = "TEXT")
    private String payload;        // JSON

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
```

### Publishing Flow

```
@Transactional
public void returnLending(...) {
    // 1. Update lending
    lending.setReturned(comment, rating);
    lendingRepository.save(lending);

    // 2. Create outbox event (same transaction)
    OutboxEvent event = new OutboxEvent(
        "LENDING",
        lending.getNumber(),
        "RETURNED",
        toJson(lendingReturnedEvent)
    );
    outboxRepository.save(event);
}

// Separate process
@Scheduled(fixedDelay = 1000)
public void publishPendingEvents() {
    List<OutboxEvent> pending = outboxRepository.findUnpublished();
    for (OutboxEvent event : pending) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());
        event.setPublishedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }
}
```

## Alternatives Considered

1. **Direct RabbitMQ Publishing**: Rejected - no atomicity guarantee
2. **Two-Phase Commit (2PC)**: Rejected - too complex, poor performance
3. **Change Data Capture (CDC)**: Future option - requires Debezium setup

## References

- Outbox Pattern: https://microservices.io/patterns/data/transactional-outbox.html
- Reliable Messaging: https://www.enterpriseintegrationpatterns.com/patterns/messaging/GuaranteedMessaging.html
