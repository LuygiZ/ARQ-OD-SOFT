package pt.psoft.shared.dto.lending;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Lending data transfer between services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LendingDTO {
    private Long id;
    private String lendingNumber;
    private String bookId;
    private Long readerId;
    private String readerNumber;
    private LocalDate startDate;
    private LocalDate limitDate;
    private LocalDate returnedDate;
    private String comment;
    private Integer rating;
    private Integer daysOverdue;
    private Integer fineAmountInCents;
    private Long version;
}
