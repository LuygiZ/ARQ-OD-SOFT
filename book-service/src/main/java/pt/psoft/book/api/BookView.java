package pt.psoft.book.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Changed authors from List<String> (names) to List<Long> (IDs)
 * for proper Feign Client mapping to BookDTO.authorIds
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Book view response")
public class BookView {

    @Schema(description = "Book ISBN", example = "9780132350884")
    private String isbn;

    @Schema(description = "Book title", example = "Clean Code")
    private String title;

    @Schema(description = "Book description")
    private String description;

    @Schema(description = "Genre name", example = "Programming")
    private String genre;

    /**
     * Changed from List<String> (names) to List<Long> (IDs)
     * Allows Feign Client to map directly to BookDTO.authorIds
     */
    @Schema(description = "Author IDs", example = "[1, 2, 3]")
    private List<Long> authors;

    @Schema(description = "Photo URI")
    private String photoURI;

    @Schema(description = "Version for optimistic locking")
    private Long version;
}