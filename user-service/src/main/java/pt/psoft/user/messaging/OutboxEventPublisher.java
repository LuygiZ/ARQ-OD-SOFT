package pt.psoft.user.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.user.repositories.jpa.OutboxRepository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls Outbox table and publishes events to RabbitMQ
 * Implements Transactional Outbox Pattern
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "lms.events";
    private static final int MAX_RETRIES = 3;

    @Scheduled(fixedDelay = 1000) // Poll every 1 second
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                String routingKey = buildRoutingKey(event);
                log.debug("Publishing to exchange: {}, routing key: {}, payload: {}",
                        EXCHANGE, routingKey, event.getPayload());

                rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event.getPayload());

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("Successfully published event: {} for aggregate: {}",
                        event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to publish event: {} for aggregate: {}. Error: {}",
                        event.getEventType(), event.getAggregateId(), e.getMessage());

                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());

                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxStatus.FAILED);
                    log.error("Event marked as FAILED after {} retries: {} for aggregate: {}",
                            MAX_RETRIES, event.getEventType(), event.getAggregateId());
                }

                outboxRepository.save(event);
            }
        }
    }

    private String buildRoutingKey(OutboxEvent event) {
        // Pattern: catalog.{aggregate}.{event}
        // Example: catalog.user.created
        return String.format("catalog.%s.%s",
                event.getAggregateType().toLowerCase(),
                event.getEventType().toLowerCase());
    }
}
