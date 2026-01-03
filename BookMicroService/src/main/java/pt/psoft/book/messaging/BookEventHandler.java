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

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventHandler {

    private final BookQueryRepository bookQueryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookCreated(BookCreatedEvent event) {
        log.info("Handling BookCreated event for ISBN: {}", event.getIsbn());

        try {
            // Convert authorIds to comma-separated string
            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String authorNames = event.getAuthorNames() != null
                    ? String.join(",", event.getAuthorNames())
                    : "";

            log.debug("Creating BookReadModel: authorIds={}, authorNames={}", authorIds, authorNames);

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
            log.info("BookReadModel created for ISBN: {} with authors: {}", event.getIsbn(), authorNames);

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

            // Convert authorIds to comma-separated string
            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // ✅ FIXED: Use authorNames from event if present, otherwise keep existing
            String authorNames = event.getAuthorNames() != null
                    ? String.join(",", event.getAuthorNames())
                    : readModel.getAuthorNames(); // Keep existing if not provided

            log.debug("Updating BookReadModel: authorIds={}, authorNames={}", authorIds, authorNames);

            readModel.updateFromEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getGenre(),
                    authorNames, // ✅ FIXED: Now uses event data or keeps existing
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