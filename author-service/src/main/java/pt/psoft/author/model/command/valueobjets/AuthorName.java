package pt.psoft.author.model.command.valueobjets;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Value Object representing an Author's Name
 */
@Embeddable
@Getter
public class AuthorName {

    public static final int MAX_LENGTH = 150;

    @Column(name = "name", nullable = false, length = MAX_LENGTH)
    @NotBlank(message = "Name is mandatory")
    @Size(min = 1, max = MAX_LENGTH, message = "Name must be between 1 and " + MAX_LENGTH + " characters")
    private String name;

    protected AuthorName() {
        // For JPA
    }

    public AuthorName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH + " characters");
        }
        this.name = name.trim();
    }

    public String getValue() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}