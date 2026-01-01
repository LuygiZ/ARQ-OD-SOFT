package pt.psoft.shared.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Review data transfer between services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private String bookId;
    private Long readerId;
    private String lendingNumber;
    private String comment;
    private Integer rating;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
