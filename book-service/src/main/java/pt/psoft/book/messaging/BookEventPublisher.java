package pt.psoft.book.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.psoft.book.repositories.OutboxRepository;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.utils.JsonUtils;

/**
 * Publishes Book domain events to Outbox table
 * Following GenreEventPublisher pattern
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventPublisher {

    private final OutboxRepository outboxRepository;

    public void publishBookCreated(BookCreatedEvent event) {
        log.info("Publishing BookCreated event for ISBN: {}", event.getIsbn());

        OutboxEvent outboxEvent = new OutboxEvent(
                "BOOK",
                event.getIsbn(),
                "CREATED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("BookCreated event saved to outbox: {}", event.getIsbn());
    }

    public void publishBookUpdated(BookUpdatedEvent event) {
        log.info("Publishing BookUpdated event for ISBN: {}", event.getIsbn());

        OutboxEvent outboxEvent = new OutboxEvent(
                "BOOK",
                event.getIsbn(),
                "UPDATED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("BookUpdated event saved to outbox: {}", event.getIsbn());
    }

    public void publishBookDeleted(BookDeletedEvent event) {
        log.info("Publishing BookDeleted event for ISBN: {}", event.getIsbn());

        OutboxEvent outboxEvent = new OutboxEvent(
                "BOOK",
                event.getIsbn(),
                "DELETED",
                JsonUtils.toJson(event)
        );

        outboxRepository.save(outboxEvent);
        log.debug("BookDeleted event saved to outbox: {}", event.getIsbn());
    }
}