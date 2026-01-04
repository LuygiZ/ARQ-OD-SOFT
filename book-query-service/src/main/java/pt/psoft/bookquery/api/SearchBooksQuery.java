package pt.psoft.bookquery.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Query parameters for book search")
public class SearchBooksQuery {

    @Schema(description = "Title to search (partial match)")
    private String title;

    @Schema(description = "Genre name to filter")
    private String genre;

    @Schema(description = "Author name to search (partial match)")
    private String authorName;
}
