package pt.psoft.book.model.command;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Value Object for ISBN (following monolith pattern)
 */
@Embeddable
@EqualsAndHashCode
public class Isbn implements Serializable {

    @NotNull
    @Size(min = 10, max = 13)
    @Column(name = "isbn", length = 13, unique = true, nullable = false)
    private String isbn;

    protected Isbn() {
        // JPA
    }

    public Isbn(String isbn) {
        if (!isValidIsbn(isbn)) {
            throw new IllegalArgumentException("Invalid ISBN format or checksum : " + isbn);
        }
        this.isbn = isbn;
    }

    private static boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }

        String clean = isbn.replaceAll("[\\s-]", "");
        return clean.length() == 10 ? isValidIsbn10(clean) : isValidIsbn13(clean);
    }

    private static boolean isValidIsbn10(String isbn) {
        if (!isbn.matches("\\d{9}[\\dX]")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (isbn.charAt(i) - '0') * (10 - i);
        }

        char lastChar = isbn.charAt(9);
        int lastDigit = (lastChar == 'X') ? 10 : lastChar - '0';
        sum += lastDigit;

        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        if (!isbn.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int checksum = 10 - (sum % 10);
        if (checksum == 10) {
            checksum = 0;
        }

        return checksum == (isbn.charAt(12) - '0');
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public String toString() {
        return isbn;
    }
}
