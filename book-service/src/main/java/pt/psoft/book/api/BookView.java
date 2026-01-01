package pt.psoft.book.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Schema(description = "Author names (denormalized)")
    private List<String> authors;

    @Schema(description = "Photo URI")
    private String photoURI;

    @Schema(description = "Average rating (0-10)", example = "8.5")
    private Double averageRating;

    @Schema(description = "Total number of reviews", example = "42")
    private Integer totalReviews;

    @Schema(description = "Version for optimistic locking")
    private Long version;
}