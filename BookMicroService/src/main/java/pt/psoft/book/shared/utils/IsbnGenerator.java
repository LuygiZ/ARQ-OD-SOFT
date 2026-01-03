package pt.psoft.book.shared.utils;

import java.util.Random;

public class IsbnGenerator {

    private static final Random random = new Random();
    private static final String ISBN_PREFIX = "978";
    private static final String REGISTRATION_GROUP = "0";

    public static String generateValidIsbn() {
        // ✅ FIXED: Generate 8 random digits (not 9!)
        String publisherAndTitle = generateRandomDigits(8);

        // Combine first 12 digits: 978 + 0 + 8 digits = 12 digits
        String isbnWithoutChecksum = ISBN_PREFIX + REGISTRATION_GROUP + publisherAndTitle;

        // Calculate check digit
        int checkDigit = calculateIsbn13CheckDigit(isbnWithoutChecksum);

        // ✅ FIXED: Concatenate without hyphens (13 digits total)
        return isbnWithoutChecksum + checkDigit;
    }

    /**
     * Generates a random string of digits
     * @param length Number of digits to generate
     * @return String of random digits
     */
    private static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static int calculateIsbn13CheckDigit(String isbnWithoutChecksum) {
        int sum = 0;
        for (int i = 0; i < isbnWithoutChecksum.length(); i++) {
            int digit = Character.getNumericValue(isbnWithoutChecksum.charAt(i));
            // Index 0,2,4,6,8,10 → multiply by 1
            // Index 1,3,5,7,9,11 → multiply by 3
            int multiplier = (i % 2 == 0) ? 1 : 3;
            sum += digit * multiplier;
        }

        // Calculate check digit (same as Isbn.java)
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }

    public static boolean isValidIsbn13(String isbn) {
        // Remove hyphens and spaces
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");

        // Must be exactly 13 digits
        if (cleanIsbn.length() != 13 || !cleanIsbn.matches("\\d+")) {
            return false;
        }

        // Calculate expected check digit
        String isbnWithoutChecksum = cleanIsbn.substring(0, 12);
        int calculatedCheckDigit = calculateIsbn13CheckDigit(isbnWithoutChecksum);
        int providedCheckDigit = Character.getNumericValue(cleanIsbn.charAt(12));

        return calculatedCheckDigit == providedCheckDigit;
    }
}