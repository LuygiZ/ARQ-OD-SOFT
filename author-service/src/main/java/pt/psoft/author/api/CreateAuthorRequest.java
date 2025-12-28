package pt.psoft.author.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Author
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new Author")
public class CreateAuthorRequest {

    @NotBlank(message = "Name is mandatory")
    @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters")
    @Schema(description = "Author's full name", example = "Robert C. Martin")
    private String name;

    @NotBlank(message = "Bio is mandatory")
    @Size(min = 1, max = 4096, message = "Bio must be between 1 and 4096 characters")
    @Schema(description = "Author's biography", example = "Software engineer and author of Clean Code")
    private String bio;

    @Size(max = 512, message = "Photo URI cannot exceed 512 characters")
    @Schema(description = "URL to author's photo", example = "https://example.com/authors/uncle-bob.jpg")
    private String photoURI;
}