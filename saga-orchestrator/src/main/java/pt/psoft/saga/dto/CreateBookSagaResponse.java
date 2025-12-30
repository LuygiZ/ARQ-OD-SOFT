package pt.psoft.saga.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.psoft.saga.model.SagaState;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Book creation saga
 * Returns aggregated information about Genre, Author, and Book
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response from Book creation saga")
public class CreateBookSagaResponse {

    @Schema(description = "Saga ID", example = "saga-123e4567-e89b-12d3-a456-426614174000")
    private String sagaId;

    @Schema(description = "Saga state")
    private SagaState state;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the saga started")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the saga completed")
    private LocalDateTime completedAt;

    @Schema(description = "Created Genre")
    private GenreResponse genre;

    @Schema(description = "Created Author")
    private AuthorResponse author;

    @Schema(description = "Created Book")
    private BookResponse book;

    @Schema(description = "Error message if saga failed")
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenreResponse {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorResponse {
        private Long authorNumber;
        private String name;
        private String bio;
        private String photoURI;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookResponse {
        private String isbn;
        private String title;
        private String description;
        private String genre;
        private List<Long> authors;  // Author numbers
    }
}