package pt.psoft.lending.model.command.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Value Object for Lending Number
 * Format: YEAR/SEQUENCE (e.g., 2024/1, 2024/2, etc.)
 */
@Embeddable
@Getter
public class LendingNumber {

    @Column(name = "lending_year", nullable = false)
    private int year;

    @Column(name = "lending_sequence", nullable = false)
    private int sequence;

    protected LendingNumber() {
        // For JPA
    }

    public LendingNumber(int sequence) {
        if (sequence < 1) {
            throw new IllegalArgumentException("Sequence must be positive");
        }
        this.year = LocalDate.now().getYear();
        this.sequence = sequence;
    }

    public LendingNumber(int year, int sequence) {
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }
        if (sequence < 1) {
            throw new IllegalArgumentException("Sequence must be positive");
        }
        this.year = year;
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return year + "/" + sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LendingNumber that = (LendingNumber) o;
        return year == that.year && sequence == that.sequence;
    }

    @Override
    public int hashCode() {
        return 31 * year + sequence;
    }

    /**
     * Parse lending number from string format "YEAR/SEQUENCE"
     */
    public static LendingNumber parse(String lendingNumber) {
        if (lendingNumber == null || !lendingNumber.contains("/")) {
            throw new IllegalArgumentException("Invalid lending number format: " + lendingNumber);
        }
        String[] parts = lendingNumber.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid lending number format: " + lendingNumber);
        }
        try {
            int year = Integer.parseInt(parts[0]);
            int sequence = Integer.parseInt(parts[1]);
            return new LendingNumber(year, sequence);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid lending number format: " + lendingNumber);
        }
    }
}
