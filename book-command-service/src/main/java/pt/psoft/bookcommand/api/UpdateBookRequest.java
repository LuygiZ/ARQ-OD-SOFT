package pt.psoft.bookcommand.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Book")
public class UpdateBookRequest {

    @Size(min = 1, max = 128)
    @Schema(description = "Book title (optional)")
    private String title;

    @Size(max = 4096)
    @Schema(description = "Book description (optional)")
    private String description;

    @Schema(description = "Genre name (optional)")
    private String genre;

    @Size(min = 1)
    @Schema(description = "List of author IDs (optional)")
    private List<Long> authorIds;

    @Schema(description = "Photo URI (optional)")
    private String photoURI;
}
