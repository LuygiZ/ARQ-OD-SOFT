package pt.psoft.bookquery.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Review view response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Book review response")
public class ReviewView {

    @Schema(description = "Review ID")
    private Long id;

    @Schema(description = "Lending number that originated the review")
    private String lendingNumber;

    @Schema(description = "Book ISBN")
    private String isbn;

    @Schema(description = "Reader number")
    private String readerNumber;

    @Schema(description = "Review comment")
    private String comment;

    @Schema(description = "Rating (0-10)")
    private Integer rating;

    @Schema(description = "Date when the book was returned")
    private LocalDate returnDate;
}
