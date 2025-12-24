package pt.psoft.genre.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.genre.repositories.OutboxRepository;
import pt.psoft.shared.events.DomainEvent;
import pt.psoft.shared.messaging.EventPublisher;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;
import pt.psoft.shared.utils.JsonUtils;

/**
 * Publica eventos via Outbox Pattern
 * Garante atomicidade entre DB update e event publishing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenreEventPublisher implements EventPublisher {

    private final OutboxRepository outboxRepository;

    @Override
    @Transactional
    public void publish(DomainEvent event) {
        log.debug("Saving event to outbox: {}", event.getEventType());

        // Serializar event para JSON
        String payload = JsonUtils.toJson(event);

        // Criar outbox event
        OutboxEvent outboxEvent = new OutboxEvent(
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                payload
        );

        // Salvar no outbox (mesma transação que a entidade)
        outboxRepository.save(outboxEvent);

        log.debug("Event saved to outbox: {}", outboxEvent.getId());
    }

    @Override
    @Transactional
    public void publish(DomainEvent event, String routingKey) {
        // Por agora, ignorar routing key (será usado pelo publisher)
        publish(event);
    }
}