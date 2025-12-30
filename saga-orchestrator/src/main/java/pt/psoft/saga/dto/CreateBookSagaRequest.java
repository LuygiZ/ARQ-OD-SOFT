package pt.psoft.saga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a complete Book entry via Saga
 * Aggregates Genre, Author, and Book data in a single request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a Book with Author and Genre via Saga")
public class CreateBookSagaRequest {

    @Valid
    @NotNull(message = "Genre information is required")
    @Schema(description = "Genre information")
    private GenreData genre;

    @Valid
    @NotNull(message = "Author information is required")
    @Schema(description = "Author information")
    private AuthorData author;

    @Valid
    @NotNull(message = "Book information is required")
    @Schema(description = "Book information")
    private BookData book;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenreData {
        @Schema(description = "Genre name", example = "Science Fiction")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorData {
        @Schema(description = "Author's full name", example = "Isaac Asimov")
        private String name;

        @Schema(description = "Author's biography", example = "American writer and professor of biochemistry")
        private String bio;

        @Schema(description = "URL to author's photo")
        private String photoURI;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookData {
        @Schema(description = "Book title", example = "Foundation")
        private String title;

        @Schema(description = "Book description")
        private String description;

        @Schema(description = "Genre name for the book", example = "Science Fiction")
        private String genreName;  // We need this to pass to Book Service

        @Schema(description = "URL to book cover photo")
        private String photoURI;
    }
}