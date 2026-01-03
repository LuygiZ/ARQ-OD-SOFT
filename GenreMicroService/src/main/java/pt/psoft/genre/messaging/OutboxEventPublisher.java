package pt.psoft.genre.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.genre.repositories.OutboxRepository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background job que publica eventos pending do Outbox para RabbitMQ
 * Executa a cada 1 segundo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "lms.events";
    private static final int MAX_RETRIES = 3;

    @Scheduled(fixedDelay = 1000) // Every 1 second
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Publishing {} pending events from outbox", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Determinar routing key baseado no event type
                String routingKey = buildRoutingKey(event);

                // Publicar para RabbitMQ
                rabbitTemplate.convertAndSend(
                        EXCHANGE,
                        routingKey,
                        event.getPayload()
                );

                // Marcar como publicado
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("Event published: {} ({})", event.getEventType(), event.getId());

            } catch (Exception e) {
                log.error("Failed to publish event: {} ({})",
                        event.getEventType(), event.getId(), e);

                // Incrementar retry count
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());

                // Se exceder max retries, marcar como FAILED
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxStatus.FAILED);
                    log.error("Event marked as FAILED after {} retries: {}",
                            MAX_RETRIES, event.getId());
                }

                outboxRepository.save(event);
            }
        }
    }

    private String buildRoutingKey(OutboxEvent event) {
        // catalog.genre.created
        // catalog.genre.updated
        // catalog.genre.deleted
        return String.format("catalog.%s.%s",
                event.getAggregateType().toLowerCase(),
                event.getEventType().toLowerCase().replace(event.getAggregateType().toLowerCase(), "")
        );
    }
}