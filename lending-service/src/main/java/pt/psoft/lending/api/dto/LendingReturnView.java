package pt.psoft.lending.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * View DTO for Lending Return response
 * Includes review details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lending return confirmation with review")
public class LendingReturnView {

    @Schema(description = "Lending number", example = "2024/1")
    private String lendingNumber;

    @Schema(description = "Return date")
    private LocalDate returnDate;

    @Schema(description = "Days overdue (0 if on time)")
    private Integer daysOverdue;

    @Schema(description = "Fine amount in cents (null if no fine)")
    private Integer fineAmountInCents;

    @Schema(description = "Review details")
    private ReviewInfo review;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Review information")
    public static class ReviewInfo {
        @Schema(description = "Comment about the book")
        private String comment;

        @Schema(description = "Rating from 0 to 10")
        private Integer rating;
    }
}
