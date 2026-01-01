package pt.psoft.lending.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new lending
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new book lending")
public class CreateLendingRequest {

    @NotBlank(message = "Book ISBN is required")
    @Schema(description = "ISBN of the book to lend", example = "9782826012092")
    private String isbn;

    @NotBlank(message = "Reader number is required")
    @Schema(description = "Reader number", example = "2024/1")
    private String readerNumber;
}
