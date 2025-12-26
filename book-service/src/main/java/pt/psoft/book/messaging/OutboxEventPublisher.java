package pt.psoft.book.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.book.repositories.OutboxRepository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.util.List;

/**
 * Polls Outbox table and publishes events to RabbitMQ
 * Following OutboxEventPublisher pattern from genre-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "lms.events";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Scheduled(fixedDelayString = "${app.outbox.scheduler.fixed-delay:1000}",
            initialDelayString = "${app.outbox.scheduler.initial-delay:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                event.markAsPublished();
                outboxRepository.save(event);
                log.info("Successfully published event: {} for aggregate: {}",
                        event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                event.incrementRetryCount();

                if (event.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                    event.markAsFailed();
                    log.error("Failed to publish event after {} attempts: {} for aggregate: {}",
                            MAX_RETRY_ATTEMPTS, event.getEventType(), event.getAggregateId(), e);
                } else {
                    log.warn("Failed to publish event (attempt {}/{}): {} for aggregate: {}",
                            event.getRetryCount(), MAX_RETRY_ATTEMPTS,
                            event.getEventType(), event.getAggregateId(), e);
                }

                outboxRepository.save(event);
            }
        }
    }

    private void publishEvent(OutboxEvent event) {
        String routingKey = String.format("catalog.%s.%s",
                event.getAggregateType().toLowerCase(),
                event.getEventType().toLowerCase());

        log.debug("Publishing to exchange: {}, routing key: {}, payload: {}",
                EXCHANGE, routingKey, event.getPayload());

        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event.getPayload());
    }
}