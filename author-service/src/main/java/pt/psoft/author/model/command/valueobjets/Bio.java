package pt.psoft.author.model.command.valueobjets;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Value Object representing an Author's Biography
 */
@Embeddable
@Getter
public class Bio {

    public static final int MAX_LENGTH = 4096;

    @Column(name = "bio", nullable = false, length = MAX_LENGTH)
    @NotBlank(message = "Bio is mandatory")
    @Size(min = 1, max = MAX_LENGTH, message = "Bio must be between 1 and " + MAX_LENGTH + " characters")
    private String bio;

    protected Bio() {
        // For JPA
    }

    public Bio(String bio) {
        if (bio == null || bio.isBlank()) {
            throw new IllegalArgumentException("Bio cannot be null or blank");
        }
        if (bio.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Bio cannot exceed " + MAX_LENGTH + " characters");
        }
        this.bio = bio.trim();
    }

    public String getValue() {
        return bio;
    }

    @Override
    public String toString() {
        return bio;
    }
}