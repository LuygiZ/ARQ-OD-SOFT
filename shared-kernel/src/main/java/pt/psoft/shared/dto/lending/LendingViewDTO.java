package pt.psoft.shared.dto.lending;

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
public class LendingViewDTO {
    private String lendingNumber;
    private String bookIsbn;
    private String bookTitle;
    private String readerNumber;
    private String readerName;
    private LocalDate startDate;
    private LocalDate limitDate;
    private LocalDate returnedDate;
    private Integer daysUntilReturn;
    private Integer daysOverdue;
    private Integer fineAmountInCents;
    private Long version;
}
