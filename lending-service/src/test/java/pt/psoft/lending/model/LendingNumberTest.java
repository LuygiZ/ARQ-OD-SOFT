package pt.psoft.lending.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.lending.model.command.valueobjects.LendingNumber;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LendingNumber value object
 */
@DisplayName("LendingNumber Tests")
class LendingNumberTest {

    @Test
    @DisplayName("Should create lending number with sequence")
    void shouldCreateLendingNumberWithSequence() {
        LendingNumber lendingNumber = new LendingNumber(1);

        assertEquals(LocalDate.now().getYear(), lendingNumber.getYear());
        assertEquals(1, lendingNumber.getSequence());
        assertEquals(LocalDate.now().getYear() + "/1", lendingNumber.toString());
    }

    @Test
    @DisplayName("Should create lending number with year and sequence")
    void shouldCreateLendingNumberWithYearAndSequence() {
        LendingNumber lendingNumber = new LendingNumber(2024, 5);

        assertEquals(2024, lendingNumber.getYear());
        assertEquals(5, lendingNumber.getSequence());
        assertEquals("2024/5", lendingNumber.toString());
    }

    @Test
    @DisplayName("Should throw exception for invalid sequence")
    void shouldThrowExceptionForInvalidSequence() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(0));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(-1));
    }

    @Test
    @DisplayName("Should throw exception for invalid year")
    void shouldThrowExceptionForInvalidYear() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(1999, 1));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(2101, 1));
    }

    @Test
    @DisplayName("Should parse lending number from string")
    void shouldParseLendingNumberFromString() {
        LendingNumber lendingNumber = LendingNumber.parse("2024/10");

        assertEquals(2024, lendingNumber.getYear());
        assertEquals(10, lendingNumber.getSequence());
    }

    @Test
    @DisplayName("Should throw exception for invalid format")
    void shouldThrowExceptionForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> LendingNumber.parse("2024"));
        assertThrows(IllegalArgumentException.class, () -> LendingNumber.parse("invalid"));
        assertThrows(IllegalArgumentException.class, () -> LendingNumber.parse(null));
    }

    @Test
    @DisplayName("Should be equal for same year and sequence")
    void shouldBeEqualForSameYearAndSequence() {
        LendingNumber ln1 = new LendingNumber(2024, 1);
        LendingNumber ln2 = new LendingNumber(2024, 1);

        assertEquals(ln1, ln2);
        assertEquals(ln1.hashCode(), ln2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal for different values")
    void shouldNotBeEqualForDifferentValues() {
        LendingNumber ln1 = new LendingNumber(2024, 1);
        LendingNumber ln2 = new LendingNumber(2024, 2);

        assertNotEquals(ln1, ln2);
    }
}
