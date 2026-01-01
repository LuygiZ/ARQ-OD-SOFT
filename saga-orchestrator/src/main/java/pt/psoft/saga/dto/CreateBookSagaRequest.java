package pt.psoft.saga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Book creation saga
 * Supports creating multiple new authors AND/OR using existing author IDs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a complete book catalog entry via Saga")
public class CreateBookSagaRequest {

    @Valid
    @NotNull
    @Schema(description = "Genre data (will be created if not exists)")
    private GenreData genre;

    @Schema(description = "List of NEW authors to create (optional)")
    private List<AuthorData> newAuthors;

    @Schema(description = "List of EXISTING author IDs to associate with book (optional)")
    private List<Long> existingAuthorIds;

    @Valid
    @NotNull
    @Schema(description = "Book data")
    private BookData book;

    /**
     * Genre data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreData {
        @NotNull
        @Schema(description = "Genre name", example = "Science Fiction")
        private String name;
    }

    /**
     * Author data for creating NEW authors
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorData {
        @NotNull
        @Schema(description = "Author name", example = "Isaac Asimov")
        private String name;

        @Schema(description = "Author biography", example = "American writer and professor of biochemistry")
        private String bio;

        @Schema(description = "Author photo URI", example = "https://example.com/asimov.jpg")
        private String photoURI;
    }

    /**
     * Book data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookData {
        @NotNull
        @Schema(description = "Book title", example = "Foundation")
        private String title;

        @Schema(description = "Book description", example = "A science fiction novel")
        private String description;

        @NotNull
        @Schema(description = "Genre name (must match genre.name)", example = "Science Fiction")
        private String genreName;

        @Schema(description = "Photo URI", example = "https://example.com/foundation.jpg")
        private String photoURI;
    }
}