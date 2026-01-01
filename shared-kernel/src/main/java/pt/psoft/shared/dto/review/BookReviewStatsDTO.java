package pt.psoft.shared.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Book Review Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookReviewStatsDTO {
    private String bookId;
    private String bookTitle;
    private Double averageRating;
    private Integer totalReviews;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    private Integer zeroStarCount;
}
