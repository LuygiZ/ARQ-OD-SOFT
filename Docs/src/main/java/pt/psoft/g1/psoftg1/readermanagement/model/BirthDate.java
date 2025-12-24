package pt.psoft.g1.psoftg1.readermanagement.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

public class BirthDate {
    private final LocalDate birthDate;

    private static final String DATE_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    private static final int DEFAULT_MINIMUM_AGE = 12;

    public BirthDate(int year, int month, int day)
    {
        this.birthDate = validateDate(LocalDate.of(year, month, day), DEFAULT_MINIMUM_AGE);
    }

    public BirthDate(String birthDate)
    {
        if (!birthDate.matches(DATE_FORMAT_REGEX))
        {
            throw new IllegalArgumentException("Provided birth date is not in a valid format. Use yyyy-MM-dd");
        }

        String[] parts = birthDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        this.birthDate = validateDate(LocalDate.of(year, month, day), DEFAULT_MINIMUM_AGE);
    }

    private LocalDate validateDate(LocalDate date, int minimumAge)
    {
        LocalDate minDate = LocalDate.now().minusYears(minimumAge);
        if (date.isAfter(minDate))
        {
            throw new IllegalArgumentException("User must be at least " + minimumAge + " years old");
        }
        return date;
    }

    // Getter
    public LocalDate getBirthDate()
    {
        return birthDate;
    }

    // Helper
    public String toString()
    {
        return birthDate.toString();
    }
}

