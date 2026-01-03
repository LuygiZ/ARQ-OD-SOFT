package pt.psoft.author.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import pt.psoft.author.model.query.AuthorReadModel;
import pt.psoft.author.repositories.mongo.AuthorQueryRepository;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;

/**
 * Event Handler for Author Domain Events
 *
 * Synchronizes PostgreSQL (Command Model) → MongoDB (Read Model)
 * This implements the CQRS pattern with Polyglot Persistence
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorEventHandler {

    private final AuthorQueryRepository authorQueryRepository;

    /**
     * Handle Author Created Event
     * Creates a new document in MongoDB
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CacheEvict(value = {"authors", "authors-search", "authors-all"}, allEntries = true)
    public void handleAuthorCreated(AuthorCreatedEvent event) {
        log.info("📝 [MONGODB] Handling AuthorCreatedEvent for author number: {}",
                event.getAuthorNumber());

        try {
            AuthorReadModel readModel = AuthorReadModel.builder()
                    .authorNumber(event.getAuthorNumber())
                    .name(event.getName())
                    .bio(event.getBio())
                    .photoURI(event.getPhotoURI())
                    .version(0L)
                    .createdAt(event.getTimestamp())
                    .updatedAt(event.getTimestamp())
                    .build();

            authorQueryRepository.save(readModel);

            log.info("✅ [MONGODB] Created read model for author: {} (ID: {})",
                    event.getName(), event.getAuthorNumber());
        } catch (Exception e) {
            log.error("❌ [MONGODB] Failed to create read model for author number: {}",
                    event.getAuthorNumber(), e);
            throw e;
        }
    }

    /**
     * Handle Author Updated Event
     * Updates the document in MongoDB
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CacheEvict(value = {"authors", "authors-search", "authors-all"}, allEntries = true)
    public void handleAuthorUpdated(AuthorUpdatedEvent event) {
        log.info("📝 [MONGODB] Handling AuthorUpdatedEvent for author number: {}",
                event.getAuthorNumber());

        try {
            AuthorReadModel readModel = authorQueryRepository.findByAuthorNumber(event.getAuthorNumber())
                    .orElseGet(() -> {
                        log.warn("⚠️  [MONGODB] Read model not found for author number: {}. Creating new one.",
                                event.getAuthorNumber());
                        AuthorReadModel newModel = new AuthorReadModel();
                        newModel.setCreatedAt(event.getTimestamp());
                        return newModel;
                    });

            readModel.updateFromEvent(
                    event.getAuthorNumber(),
                    event.getName(),
                    event.getBio(),
                    event.getPhotoURI(),
                    event.getVersion(),
                    event.getTimestamp()
            );

            authorQueryRepository.save(readModel);

            log.info("✅ [MONGODB] Updated read model for author: {} (ID: {})",
                    event.getName(), event.getAuthorNumber());
        } catch (Exception e) {
            log.error("❌ [MONGODB] Failed to update read model for author number: {}",
                    event.getAuthorNumber(), e);
            throw e;
        }
    }

    /**
     * Handle Author Deleted Event
     * Deletes the document from MongoDB
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CacheEvict(value = {"authors", "authors-search", "authors-all"}, allEntries = true)
    public void handleAuthorDeleted(AuthorDeletedEvent event) {
        log.info("📝 [MONGODB] Handling AuthorDeletedEvent for author number: {}",
                event.getAuthorNumber());

        try {
            authorQueryRepository.deleteByAuthorNumber(event.getAuthorNumber());

            log.info("✅ [MONGODB] Deleted read model for author number: {}",
                    event.getAuthorNumber());
        } catch (Exception e) {
            log.error("❌ [MONGODB] Failed to delete read model for author number: {}",
                    event.getAuthorNumber(), e);
            throw e;
        }
    }
}