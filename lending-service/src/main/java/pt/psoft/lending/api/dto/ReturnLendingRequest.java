package pt.psoft.lending.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for returning a lending with comment and rating
 * This is the key DTO for Student C functionality
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for returning a book with review")
public class ReturnLendingRequest {

    @NotNull(message = "Comment cannot be null (but can be empty)")
    @Size(max = 1024, message = "Comment cannot exceed 1024 characters")
    @Schema(description = "Comment about the book", example = "Great book, really enjoyed reading it!")
    private String comment;

    @NotNull(message = "Rating is required")
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 10, message = "Rating must be at most 10")
    @Schema(description = "Rating from 0 to 10", example = "8", minimum = "0", maximum = "10")
    private Integer rating;
}
