package pt.psoft.book.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new Book")
public class CreateBookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 128)
    @Schema(description = "Book title", example = "Clean Code")
    private String title;

    @Size(max = 4096)
    @Schema(description = "Book description")
    private String description;

    @NotBlank(message = "Genre is required")
    @Schema(description = "Genre name", example = "Programming")
    private String genre;

    @NotNull(message = "Authors are required")
    @Size(min = 1)
    @Schema(description = "List of author IDs", example = "[1, 2]")
    private List<Long> authorIds;

    @Schema(description = "Photo URI")
    private String photoURI;
}