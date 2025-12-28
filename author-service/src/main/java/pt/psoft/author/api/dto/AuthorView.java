package pt.psoft.author.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Author response (from Read Model)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Author information")
public class AuthorView {

    @Schema(description = "Author's unique number", example = "1")
    private Long authorNumber;

    @Schema(description = "Author's full name", example = "Robert C. Martin")
    private String name;

    @Schema(description = "Author's biography", example = "Software engineer and author of Clean Code")
    private String bio;

    @Schema(description = "URL to author's photo", example = "https://example.com/authors/uncle-bob.jpg")
    private String photoURI;

    @Schema(description = "Version for optimistic locking", example = "0")
    private Long version;
}