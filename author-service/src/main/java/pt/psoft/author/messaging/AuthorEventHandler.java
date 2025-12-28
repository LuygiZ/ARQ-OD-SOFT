package pt.psoft.author.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.psoft.author.model.query.AuthorReadModel;
import pt.psoft.author.repositories.AuthorQueryRepository;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;

/**
 * Handles Author domain events and updates the Read Model
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorEventHandler {

    private final AuthorQueryRepository authorQueryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Caching(evict = {
            @CacheEvict(value = "authors", key = "#event.authorNumber"),
            @CacheEvict(value = "authors-search", allEntries = true),
            @CacheEvict(value = "authors-all", allEntries = true)
    })
    public void handleAuthorCreated(AuthorCreatedEvent event) {
        log.info("Handling AuthorCreated event for number: {}", event.getAuthorNumber());

        AuthorReadModel readModel = new AuthorReadModel(
                event.getAuthorNumber(),
                event.getName(),
                event.getBio(),
                event.getPhotoURI(),
                0L // New author always starts at version 0
        );

        authorQueryRepository.save(readModel);
        log.info("AuthorReadModel created for number: {}", event.getAuthorNumber());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Caching(evict = {
            @CacheEvict(value = "authors", key = "#event.authorNumber"),
            @CacheEvict(value = "authors-search", allEntries = true),
            @CacheEvict(value = "authors-all", allEntries = true)
    })
    public void handleAuthorUpdated(AuthorUpdatedEvent event) {
        log.info("Handling AuthorUpdated event for number: {}", event.getAuthorNumber());

        AuthorReadModel readModel = authorQueryRepository.findByAuthorNumber(event.getAuthorNumber())
                .orElseThrow(() -> new RuntimeException("AuthorReadModel not found for number: " + event.getAuthorNumber()));

        readModel.updateFromEvent(
                event.getName(),
                event.getBio(),
                event.getPhotoURI(),
                event.getVersion()
        );

        authorQueryRepository.save(readModel);
        log.info("AuthorReadModel updated for number: {}", event.getAuthorNumber());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Caching(evict = {
            @CacheEvict(value = "authors", key = "#event.authorNumber"),
            @CacheEvict(value = "authors-search", allEntries = true),
            @CacheEvict(value = "authors-all", allEntries = true)
    })
    public void handleAuthorDeleted(AuthorDeletedEvent event) {
        log.info("Handling AuthorDeleted event for number: {}", event.getAuthorNumber());

        authorQueryRepository.deleteById(event.getAuthorNumber());
        log.info("AuthorReadModel deleted for number: {}", event.getAuthorNumber());
    }
}