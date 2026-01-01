package pt.psoft.book.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.psoft.book.model.query.BookReadModel;
import pt.psoft.book.model.query.BookReview;
import pt.psoft.book.repositories.BookQueryRepository;
import pt.psoft.book.repositories.BookReviewRepository;
import pt.psoft.book.model.command.BookEntity;
import pt.psoft.book.repositories.BookRepository;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookRatingUpdatedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;
import pt.psoft.shared.events.lending.LendingReturnedEvent;

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
    private final BookRepository bookCommandRepository;
    private final BookReviewRepository bookReviewRepository;
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @CacheEvict(value = "books", allEntries = true)
    public void handleBookRatingUpdated(BookRatingUpdatedEvent event) {
        log.info("Handling BookRatingUpdated event for ISBN: {}, avgRating: {}, totalReviews: {}",
                event.getIsbn(), event.getAverageRating(), event.getTotalReviews());

        try {
            // Update Command Model (BookEntity)
            bookCommandRepository.findByIsbn(event.getIsbn())
                    .ifPresent(book -> {
                        book.updateRating(event.getAverageRating(), event.getTotalReviews());
                        bookCommandRepository.save(book);
                        log.info("BookEntity rating updated for ISBN: {}", event.getIsbn());
                    });

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

        // Update Command Model
        bookCommandRepository.findByIsbn(isbn)
                .ifPresent(book -> {
                    book.updateRating(avgRating, (int) totalReviews);
                    bookCommandRepository.save(book);
                    log.info("BookEntity rating recalculated for ISBN: {}", isbn);
                });

        // Update Query Model
        bookQueryRepository.findByIsbn(isbn)
                .ifPresent(readModel -> {
                    readModel.updateRating(avgRating, (int) totalReviews);
                    bookQueryRepository.save(readModel);
                    log.info("BookReadModel rating recalculated for ISBN: {}", isbn);
                });
    }
}