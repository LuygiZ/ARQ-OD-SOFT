package pt.psoft.book.model.command;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.io.Serializable;

/**
 * Value Object for Book Description (following monolith pattern)
 */
@Embeddable
public class Description implements Serializable {

    public static final int MAX_LENGTH = 4096;

    @Size(max = MAX_LENGTH)
    @Column(name = "description", length = MAX_LENGTH)
    @Getter
    private String description;

    protected Description() {
        // JPA
    }

    public Description(String description) {
        if (description != null && description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_LENGTH + " characters");
        }
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }

    @Override
    public String toString() {
        return description;
    }
}