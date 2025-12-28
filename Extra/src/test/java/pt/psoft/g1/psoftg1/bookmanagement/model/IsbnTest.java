
package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional Opaque-Box Tests for Isbn class
 * Tests the class as a black box, focusing on input/output behavior
 */
@DisplayName("Isbn - Functional Opaque-Box Tests")
class IsbnOpaqueBoxTest {

    @Test
    @DisplayName("Should accept valid ISBN-10 with numeric check digit")
    void shouldAcceptValidIsbn10() {
        // Arrange & Act
        Isbn isbn = new Isbn("0306406152");

        // Assert
        assertNotNull(isbn);
        assertEquals("0306406152", isbn.getIsbn());
    }

    @Test
    @DisplayName("Should accept valid ISBN-10 with X check digit")
    void shouldAcceptValidIsbn10WithX() {
        // Arrange & Act
        Isbn isbn = new Isbn("043942089X");

        // Assert
        assertNotNull(isbn);
        assertEquals("043942089X", isbn.getIsbn());
    }

    @Test
    @DisplayName("Should accept valid ISBN-13")
    void shouldAcceptValidIsbn13() {
        // Arrange & Act
        Isbn isbn = new Isbn("9780306406157");

        // Assert
        assertNotNull(isbn);
        assertEquals("9780306406157", isbn.getIsbn());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0306406153",  // Invalid check digit
            "123456789X",  // Invalid ISBN-10
            "9780306406158", // Invalid ISBN-13 check digit
            "12345",       // Too short
            "abc1234567",  // Non-numeric
            "978030640615A" // Invalid character in ISBN-13
    })
    @DisplayName("Should reject invalid ISBNs")
    void shouldRejectInvalidIsbn(String invalidIsbn) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(invalidIsbn));
    }

    @Test
    @DisplayName("Should throw exception for null ISBN")
    void shouldRejectNullIsbn() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(null)
        );
        assertEquals("Isbn cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("toString should return ISBN value")
    void toStringShouldReturnIsbnValue() {
        // Arrange
        Isbn isbn = new Isbn("9780306406157");

        // Act & Assert
        assertEquals("9780306406157", isbn.toString());
    }

    @Test
    @DisplayName("Boundary: ISBN-10 with exactly 10 characters")
    void boundaryIsbn10Length() {
        // Arrange & Act
        Isbn isbn = new Isbn("0471958697");

        // Assert
        assertEquals(10, isbn.getIsbn().length());
    }

    @Test
    @DisplayName("Boundary: ISBN-13 with exactly 13 characters")
    void boundaryIsbn13Length() {
        // Arrange & Act
        Isbn isbn = new Isbn("9780471958697");

        // Assert
        assertEquals(13, isbn.getIsbn().length());
    }
}