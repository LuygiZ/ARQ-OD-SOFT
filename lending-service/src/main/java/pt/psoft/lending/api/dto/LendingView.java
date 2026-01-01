package pt.psoft.lending.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * View DTO for Lending API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lending information")
public class LendingView {

    @Schema(description = "Lending number", example = "2024/1")
    private String lendingNumber;

    @Schema(description = "Book ISBN", example = "9782826012092")
    private String bookId;

    @Schema(description = "Reader number", example = "2024/1")
    private String readerNumber;

    @Schema(description = "Start date of lending")
    private LocalDate startDate;

    @Schema(description = "Limit date for return")
    private LocalDate limitDate;

    @Schema(description = "Actual return date (null if not returned)")
    private LocalDate returnedDate;

    @Schema(description = "Days until return (null if overdue or returned)")
    private Integer daysUntilReturn;

    @Schema(description = "Days overdue (null if not overdue)")
    private Integer daysOverdue;

    @Schema(description = "Fine amount in cents (null if no fine)")
    private Integer fineAmountInCents;

    @Schema(description = "Comment left on return")
    private String comment;

    @Schema(description = "Rating given on return (0-10)")
    private Integer rating;

    @Schema(description = "Version for optimistic locking")
    private Long version;
}
