package pt.psoft.book.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.psoft.book.model.query.BookReadModel;
import pt.psoft.book.repositories.BookQueryRepository;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;

import java.util.stream.Collectors;

/**
 * Event Handler to synchronize Write Model → Read Model (CQRS)
 * Listens to domain events and updates BookReadModel accordingly
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventHandler {

    private final BookQueryRepository bookQueryRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookCreated(BookCreatedEvent event) {
        log.info("Handling BookCreated event for ISBN: {}", event.getIsbn());

        try {
            // TODO: Fetch author names from Author Service (via sync call or event)
            // For now, store only IDs as comma-separated string
            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String authorNames = ""; // TODO: Resolve author names

            BookReadModel readModel = new BookReadModel(
                    event.getIsbn(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getGenre(),
                    authorNames,
                    authorIds,
                    event.getPhotoURI(),
                    0L // Initial version
            );

            bookQueryRepository.save(readModel);
            log.info("BookReadModel created for ISBN: {}", event.getIsbn());

        } catch (Exception e) {
            log.error("Failed to handle BookCreated event for ISBN: {}", event.getIsbn(), e);
            throw new RuntimeException("Failed to synchronize read model", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookUpdated(BookUpdatedEvent event) {
        log.info("Handling BookUpdated event for ISBN: {}", event.getIsbn());

        try {
            BookReadModel readModel = bookQueryRepository.findByIsbn(event.getIsbn())
                    .orElseThrow(() -> new RuntimeException("ReadModel not found for ISBN: " + event.getIsbn()));

            // TODO: Fetch author names from Author Service
            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String authorNames = ""; // TODO: Resolve author names

            readModel.updateFromEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getGenre(),
                    authorNames,
                    authorIds,
                    event.getPhotoURI(),
                    event.getVersion()
            );

            bookQueryRepository.save(readModel);
            log.info("BookReadModel updated for ISBN: {}", event.getIsbn());

        } catch (Exception e) {
            log.error("Failed to handle BookUpdated event for ISBN: {}", event.getIsbn(), e);
            throw new RuntimeException("Failed to synchronize read model", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookDeleted(BookDeletedEvent event) {
        log.info("Handling BookDeleted event for ISBN: {}", event.getIsbn());

        try {
            bookQueryRepository.deleteById(event.getIsbn());
            log.info("BookReadModel deleted for ISBN: {}", event.getIsbn());

        } catch (Exception e) {
            log.error("Failed to handle BookDeleted event for ISBN: {}", event.getIsbn(), e);
            throw new RuntimeException("Failed to synchronize read model", e);
        }
    }
}