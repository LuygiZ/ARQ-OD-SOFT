package pt.psoft.genre.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for creating a new Book
 * Shared across all services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest implements Serializable {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 128, message = "Title must be between 1 and 128 characters")
    private String title;

    @Size(max = 4096, message = "Description cannot exceed 4096 characters")
    private String description;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotNull(message = "Authors are required")
    @Size(min = 1, message = "At least one author is required")
    private List<Long> authorIds;

    @NotNull(message = "Author names are required")
    @Size(min = 1, message = "At least one author name is required")
    private List<String> authorNames;

    private String photoURI;
}