package pt.psoft.lending.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pt.psoft.lending.model.command.LendingEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LendingEntity
 * Includes both opaque-box and transparent-box tests
 */
@DisplayName("LendingEntity Tests")
class LendingEntityTest {

    private static final String BOOK_ID = "9782826012092";
    private static final Long READER_ID = 1L;
    private static final String READER_NUMBER = "2024/1";
    private static final int LENDING_DURATION = 14;
    private static final int FINE_PER_DAY = 50;

    @Nested
    @DisplayName("Constructor Tests (Opaque-box)")
    class ConstructorTests {

        @Test
        @DisplayName("Should create lending with valid parameters")
        void shouldCreateLendingWithValidParameters() {
            LendingEntity lending = new LendingEntity(
                    BOOK_ID, READER_ID, READER_NUMBER, 1, LENDING_DURATION, FINE_PER_DAY);

            assertNotNull(lending);
            assertEquals(BOOK_ID, lending.getBookId());
            assertEquals(READER_ID, lending.getReaderId());
            assertEquals(READER_NUMBER, lending.getReaderNumber());
            assertEquals(LocalDate.now(), lending.getStartDate());
            assertEquals(LocalDate.now().plusDays(LENDING_DURATION), lending.getLimitDate());
            assertNull(lending.getReturnedDate());
            assertTrue(lending.isActive());
        }

        @Test
        @DisplayName("Should throw exception when book ID is null")
        void shouldThrowExceptionWhenBookIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new LendingEntity(null, READER_ID, READER_NUMBER, 1, LENDING_DURATION, FINE_PER_DAY));
        }

        @Test
        @DisplayName("Should throw exception when reader ID is null")
        void shouldThrowExceptionWhenReaderIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new LendingEntity(BOOK_ID, null, READER_NUMBER, 1, LENDING_DURATION, FINE_PER_DAY));
        }

        @Test
        @DisplayName("Should throw exception when reader number is null")
        void shouldThrowExceptionWhenReaderNumberIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new LendingEntity(BOOK_ID, READER_ID, null, 1, LENDING_DURATION, FINE_PER_DAY));
        }
    }

    @Nested
    @DisplayName("Return Tests (Transparent-box)")
    class ReturnTests {

        @Test
        @DisplayName("Should set returned with valid rating and comment")
        void shouldSetReturnedWithValidRatingAndComment() {
            LendingEntity lending = createTestLending();

            lending.setReturned(0L, "Great book!", 8);

            assertEquals(LocalDate.now(), lending.getReturnedDate());
            assertEquals("Great book!", lending.getComment());
            assertEquals(8, lending.getRating());
            assertFalse(lending.isActive());
        }

        @Test
        @DisplayName("Should throw exception when rating is below 0")
        void shouldThrowExceptionWhenRatingBelowZero() {
            LendingEntity lending = createTestLending();

            assertThrows(IllegalArgumentException.class, () ->
                    lending.setReturned(0L, "Comment", -1));
        }

        @Test
        @DisplayName("Should throw exception when rating is above 10")
        void shouldThrowExceptionWhenRatingAboveTen() {
            LendingEntity lending = createTestLending();

            assertThrows(IllegalArgumentException.class, () ->
                    lending.setReturned(0L, "Comment", 11));
        }

        @Test
        @DisplayName("Should accept rating at boundary 0")
        void shouldAcceptRatingAtBoundaryZero() {
            LendingEntity lending = createTestLending();

            lending.setReturned(0L, "Comment", 0);

            assertEquals(0, lending.getRating());
        }

        @Test
        @DisplayName("Should accept rating at boundary 10")
        void shouldAcceptRatingAtBoundaryTen() {
            LendingEntity lending = createTestLending();

            lending.setReturned(0L, "Comment", 10);

            assertEquals(10, lending.getRating());
        }

        @Test
        @DisplayName("Should throw exception when already returned")
        void shouldThrowExceptionWhenAlreadyReturned() {
            LendingEntity lending = createTestLending();
            lending.setReturned(0L, "First comment", 5);

            assertThrows(IllegalStateException.class, () ->
                    lending.setReturned(1L, "Second comment", 8));
        }

        @Test
        @DisplayName("Should accept empty comment")
        void shouldAcceptEmptyComment() {
            LendingEntity lending = createTestLending();

            lending.setReturned(0L, "", 5);

            assertEquals("", lending.getComment());
        }

        @Test
        @DisplayName("Should accept null rating (no rating provided)")
        void shouldAcceptNullRating() {
            LendingEntity lending = createTestLending();

            lending.setReturned(0L, "Comment", null);

            assertNull(lending.getRating());
            assertNotNull(lending.getReturnedDate());
        }
    }

    @Nested
    @DisplayName("Days Calculation Tests")
    class DaysCalculationTests {

        @Test
        @DisplayName("Should return correct days until return when not overdue")
        void shouldReturnCorrectDaysUntilReturn() {
            LendingEntity lending = createTestLending();

            Integer daysUntilReturn = lending.getDaysUntilReturn();

            assertNotNull(daysUntilReturn);
            assertEquals(LENDING_DURATION, daysUntilReturn);
        }

        @Test
        @DisplayName("Should return null for days until return when returned")
        void shouldReturnNullForDaysUntilReturnWhenReturned() {
            LendingEntity lending = createTestLending();
            lending.setReturned(0L, "Comment", 5);

            Integer daysUntilReturn = lending.getDaysUntilReturn();

            assertNull(daysUntilReturn);
        }

        @Test
        @DisplayName("Should return zero days delayed when not overdue")
        void shouldReturnZeroDaysDelayedWhenNotOverdue() {
            LendingEntity lending = createTestLending();

            int daysDelayed = lending.getDaysDelayed();

            assertEquals(0, daysDelayed);
        }

        @Test
        @DisplayName("Should return null fine amount when not overdue")
        void shouldReturnNullFineAmountWhenNotOverdue() {
            LendingEntity lending = createTestLending();

            Integer fineAmount = lending.getFineAmountInCents();

            assertNull(fineAmount);
        }
    }

    private LendingEntity createTestLending() {
        return new LendingEntity(BOOK_ID, READER_ID, READER_NUMBER, 1, LENDING_DURATION, FINE_PER_DAY);
    }
}
