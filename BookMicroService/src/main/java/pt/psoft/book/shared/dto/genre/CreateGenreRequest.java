package pt.psoft.book.shared.dto.genre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for creating a new Genre
 * Shared across all services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGenreRequest implements Serializable {

    @NotBlank(message = "Genre name is required")
    @Size(min = 1, max = 100, message = "Genre name must be between 1 and 100 characters")
    private String name;
}