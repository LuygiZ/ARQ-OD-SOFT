package pt.psoft.bookquery.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.psoft.bookquery.model.BookReadModel;
import pt.psoft.bookquery.model.BookReview;
import pt.psoft.bookquery.repositories.BookQueryRepository;
import pt.psoft.bookquery.repositories.BookReviewRepository;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookRatingUpdatedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;
import pt.psoft.shared.events.lending.LendingReturnedEvent;

import java.util.stream.Collectors;

/**
 * Event Handler to synchronize Write Model -> Read Model (CQRS)
 * Listens to domain events and updates BookReadModel accordingly
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventHandler {

    private final BookQueryRepository bookQueryRepository;
    private final BookReviewRepository bookReviewRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookCreated(BookCreatedEvent event) {
        log.info("Handling BookCreated event for ISBN: {}", event.getIsbn());

        try {
            // Store author IDs as comma-separated string
            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String authorNames = ""; // TODO: Resolve author names from Author Service

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

            String authorIds = event.getAuthorIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String authorNames = ""; // TODO: Resolve author names from Author Service

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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookRatingUpdated(BookRatingUpdatedEvent event) {
        log.info("Handling BookRatingUpdated event for ISBN: {}, avgRating: {}, totalReviews: {}",
                event.getIsbn(), event.getAverageRating(), event.getTotalReviews());

        try {
            // Update Query Model (BookReadModel)
            bookQueryRepository.findByIsbn(event.getIsbn())
                    .ifPresent(readModel -> {
                        readModel.updateRating(event.getAverageRating(), event.getTotalReviews());
                        bookQueryRepository.save(readModel);
                        log.info("BookReadModel rating updated for ISBN: {}", event.getIsbn());
                    });

        } catch (Exception e) {
            log.error("Failed to handle BookRatingUpdated event for ISBN: {}", event.getIsbn(), e);
            throw new RuntimeException("Failed to update book rating", e);
        }
    }

    /**
     * Handle LendingReturned event to create a BookReview
     * This stores individual reviews for books from lending returns
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleLendingReturned(LendingReturnedEvent event) {
        log.info("Handling LendingReturned event for lending: {}, book: {}, rating: {}",
                event.getLendingNumber(), event.getBookId(), event.getRating());

        try {
            // Only create review if there's a rating
            if (event.getRating() != null) {
                // Check if review already exists (idempotency)
                if (bookReviewRepository.existsByLendingNumber(event.getLendingNumber())) {
                    log.info("Review already exists for lending: {}", event.getLendingNumber());
                    return;
                }

                // Create new review
                BookReview review = new BookReview(
                        event.getLendingNumber(),
                        event.getBookId(),
                        event.getReaderNumber(),
                        event.getComment(),
                        event.getRating(),
                        event.getReturnDate()
                );

                bookReviewRepository.save(review);
                log.info("BookReview created for lending: {}, book: {}", event.getLendingNumber(), event.getBookId());

                // Recalculate and update book rating statistics
                updateBookRatingStats(event.getBookId());
            }

        } catch (Exception e) {
            log.error("Failed to handle LendingReturned event for lending: {}", event.getLendingNumber(), e);
            throw new RuntimeException("Failed to create book review", e);
        }
    }

    /**
     * Recalculate and update book rating statistics
     */
    private void updateBookRatingStats(String isbn) {
        Double avgRating = bookReviewRepository.getAverageRatingByIsbn(isbn);
        long totalReviews = bookReviewRepository.countByIsbn(isbn);

        // Update Query Model
        bookQueryRepository.findByIsbn(isbn)
                .ifPresent(readModel -> {
                    readModel.updateRating(avgRating, (int) totalReviews);
                    bookQueryRepository.save(readModel);
                    log.info("BookReadModel rating recalculated for ISBN: {}", isbn);
                });
    }
}
