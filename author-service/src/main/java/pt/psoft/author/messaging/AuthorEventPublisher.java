package pt.psoft.author.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.psoft.author.repositories.jpa.OutboxRepository;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.utils.JsonUtils;

/**
 * Publishes Author domain events to Outbox table
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorEventPublisher {

    private final OutboxRepository outboxRepository;

    public void publishAuthorCreated(AuthorCreatedEvent event) {
        log.info("Publishing AuthorCreated event for number: {}", event.getAuthorNumber());

        OutboxEvent outboxEvent = new OutboxEvent(
                "AUTHOR",
                event.getAuthorNumber().toString(),
                "CREATED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("AuthorCreated event saved to outbox: {}", event.getAuthorNumber());
    }

    public void publishAuthorUpdated(AuthorUpdatedEvent event) {
        log.info("Publishing AuthorUpdated event for number: {}", event.getAuthorNumber());

        OutboxEvent outboxEvent = new OutboxEvent(
                "AUTHOR",
                event.getAuthorNumber().toString(),
                "UPDATED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("AuthorUpdated event saved to outbox: {}", event.getAuthorNumber());
    }

    public void publishAuthorDeleted(AuthorDeletedEvent event) {
        log.info("Publishing AuthorDeleted event for number: {}", event.getAuthorNumber());

        OutboxEvent outboxEvent = new OutboxEvent(
                "AUTHOR",
                event.getAuthorNumber().toString(),
                "DELETED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("AuthorDeleted event saved to outbox: {}", event.getAuthorNumber());
    }
}