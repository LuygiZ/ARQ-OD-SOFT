package pt.psoft.book.model.command;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Value Object for Book Title (following monolith pattern)
 */
@Embeddable
@EqualsAndHashCode
public class Title implements Serializable {

    public static final int MAX_LENGTH = 128;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = MAX_LENGTH)
    @Column(name = "title", length = MAX_LENGTH, nullable = false)
    @Getter
    private String title;

    protected Title() {
        // JPA
    }

    public Title(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (title.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
        this.title = title.trim();
    }

    @Override
    public String toString() {
        return title;
    }
}
