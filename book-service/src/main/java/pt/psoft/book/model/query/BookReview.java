package pt.psoft.book.model.query;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Book Review entity (Query Side - CQRS)
 * Stores individual reviews from lending returns
 * Synchronized from Lending Service via Domain Events
 */
@Entity
@Table(name = "book_reviews", indexes = {
        @Index(name = "idx_review_isbn", columnList = "isbn"),
        @Index(name = "idx_review_reader", columnList = "reader_number"),
        @Index(name = "idx_review_rating", columnList = "rating"),
        @Index(name = "idx_review_date", columnList = "return_date")
})
@Getter
@Setter
@NoArgsConstructor
public class BookReview {

    private static final int MIN_RATING = 0;
    private static final int MAX_RATING = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lending_number", nullable = false, unique = true)
    private String lendingNumber;

    @Column(name = "isbn", nullable = false)
    private String isbn;  // Book ISBN

    @Column(name = "reader_number", nullable = false)
    private String readerNumber;

    @Column(name = "comment", length = 2048)
    private String comment;

    @Column(name = "rating", nullable = false)
    private Integer rating;  // 0-10

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public BookReview(String lendingNumber, String isbn, String readerNumber,
                      String comment, Integer rating, LocalDate returnDate) {
        validateLendingNumber(lendingNumber);
        validateIsbn(isbn);
        validateReaderNumber(readerNumber);
        validateRating(rating);
        validateReturnDate(returnDate);

        this.lendingNumber = lendingNumber;
        this.isbn = isbn;
        this.readerNumber = readerNumber;
        this.comment = comment;
        this.rating = rating;
        this.returnDate = returnDate;
        this.createdAt = LocalDateTime.now();
    }

    private void validateLendingNumber(String lendingNumber) {
        if (lendingNumber == null || lendingNumber.isBlank()) {
            throw new IllegalArgumentException("Lending number cannot be null or blank");
        }
    }

    private void validateIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
    }

    private void validateReaderNumber(String readerNumber) {
        if (readerNumber == null || readerNumber.isBlank()) {
            throw new IllegalArgumentException("Reader number cannot be null or blank");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
        }
    }

    private void validateReturnDate(LocalDate returnDate) {
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date cannot be null");
        }
    }
}
