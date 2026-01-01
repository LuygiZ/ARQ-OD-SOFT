package pt.psoft.shared.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * View DTO for Review API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewViewDTO {
    private Long id;
    private String bookIsbn;
    private String bookTitle;
    private String readerNumber;
    private String readerName;
    private String lendingNumber;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;
    private Long version;
}
