package pt.psoft.author.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Author
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an Author")
public class UpdateAuthorRequest {

    @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters")
    @Schema(description = "Author's full name", example = "Robert C. Martin")
    private String name;

    @Size(min = 1, max = 4096, message = "Bio must be between 1 and 4096 characters")
    @Schema(description = "Author's biography", example = "Updated biography")
    private String bio;

    @Size(max = 512, message = "Photo URI cannot exceed 512 characters")
    @Schema(description = "URL to author's photo", example = "https://example.com/authors/new-photo.jpg")
    private String photoURI;
}