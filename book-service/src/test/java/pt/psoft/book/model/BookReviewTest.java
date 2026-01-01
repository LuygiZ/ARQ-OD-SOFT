package pt.psoft.book.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pt.psoft.book.model.query.BookReview;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BookReview entity
 * Designed for mutation testing coverage
 */
class BookReviewTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create BookReview with valid parameters")
        void shouldCreateBookReviewWithValidParams() {
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Great book!",
                    8,
                    LocalDate.of(2025, 1, 15)
            );

            assertThat(review.getLendingNumber()).isEqualTo("2025/1");
            assertThat(review.getIsbn()).isEqualTo("978-0-13-468599-1");
            assertThat(review.getReaderNumber()).isEqualTo("2025/100");
            assertThat(review.getComment()).isEqualTo("Great book!");
            assertThat(review.getRating()).isEqualTo(8);
            assertThat(review.getReturnDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("Should create BookReview with null comment")
        void shouldCreateBookReviewWithNullComment() {
            BookReview review = new BookReview(
                    "2025/2",
                    "978-0-13-468599-1",
                    "2025/101",
                    null,
                    7,
                    LocalDate.of(2025, 1, 16)
            );

            assertThat(review.getComment()).isNull();
            assertThat(review.getRating()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception for null lending number")
        void shouldThrowExceptionForNullLendingNumber() {
            assertThatThrownBy(() -> new BookReview(
                    null,
                    "978-0-13-468599-1",
                    "2025/100",
                    "Comment",
                    8,
                    LocalDate.now()
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for null ISBN")
        void shouldThrowExceptionForNullIsbn() {
            assertThatThrownBy(() -> new BookReview(
                    "2025/1",
                    null,
                    "2025/100",
                    "Comment",
                    8,
                    LocalDate.now()
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for null reader number")
        void shouldThrowExceptionForNullReaderNumber() {
            assertThatThrownBy(() -> new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    null,
                    "Comment",
                    8,
                    LocalDate.now()
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Rating Validation Tests")
    class RatingValidationTests {

        @Test
        @DisplayName("Should accept minimum rating of 0")
        void shouldAcceptMinimumRating() {
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Not good",
                    0,
                    LocalDate.now()
            );

            assertThat(review.getRating()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should accept maximum rating of 10")
        void shouldAcceptMaximumRating() {
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Perfect!",
                    10,
                    LocalDate.now()
            );

            assertThat(review.getRating()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should throw exception for rating below 0")
        void shouldThrowExceptionForRatingBelowZero() {
            assertThatThrownBy(() -> new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Comment",
                    -1,
                    LocalDate.now()
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for rating above 10")
        void shouldThrowExceptionForRatingAboveTen() {
            assertThatThrownBy(() -> new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Comment",
                    11,
                    LocalDate.now()
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Return Date Tests")
    class ReturnDateTests {

        @Test
        @DisplayName("Should accept today's date")
        void shouldAcceptTodaysDate() {
            LocalDate today = LocalDate.now();
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Good",
                    7,
                    today
            );

            assertThat(review.getReturnDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("Should accept past date")
        void shouldAcceptPastDate() {
            LocalDate pastDate = LocalDate.of(2024, 6, 15);
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Good",
                    7,
                    pastDate
            );

            assertThat(review.getReturnDate()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should throw exception for null return date")
        void shouldThrowExceptionForNullReturnDate() {
            assertThatThrownBy(() -> new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "Comment",
                    8,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Comment Tests")
    class CommentTests {

        @Test
        @DisplayName("Should accept empty comment")
        void shouldAcceptEmptyComment() {
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    "",
                    5,
                    LocalDate.now()
            );

            assertThat(review.getComment()).isEmpty();
        }

        @Test
        @DisplayName("Should accept long comment")
        void shouldAcceptLongComment() {
            String longComment = "A".repeat(2000);
            BookReview review = new BookReview(
                    "2025/1",
                    "978-0-13-468599-1",
                    "2025/100",
                    longComment,
                    8,
                    LocalDate.now()
            );

            assertThat(review.getComment()).hasSize(2000);
        }
    }
}
