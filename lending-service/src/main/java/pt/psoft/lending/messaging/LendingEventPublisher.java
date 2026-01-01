package pt.psoft.lending.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.psoft.lending.repositories.jpa.OutboxRepository;
import pt.psoft.shared.events.lending.LendingCreatedEvent;
import pt.psoft.shared.events.lending.LendingReturnedEvent;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.utils.JsonUtils;

/**
 * Publishes Lending domain events to Outbox table
 * Uses Outbox Pattern for reliable event publishing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LendingEventPublisher {

    private final OutboxRepository outboxRepository;

    public void publishLendingCreated(LendingCreatedEvent event) {
        log.info("Publishing LendingCreated event for lending: {}", event.getLendingNumber());

        OutboxEvent outboxEvent = new OutboxEvent(
                "LENDING",
                event.getLendingNumber(),
                "CREATED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("LendingCreated event saved to outbox: {}", event.getLendingNumber());
    }

    public void publishLendingReturned(LendingReturnedEvent event) {
        log.info("Publishing LendingReturned event for lending: {} with rating: {}",
                event.getLendingNumber(), event.getRating());

        OutboxEvent outboxEvent = new OutboxEvent(
                "LENDING",
                event.getLendingNumber(),
                "RETURNED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("LendingReturned event saved to outbox: {}", event.getLendingNumber());
    }
}
