package pt.psoft.book.genremanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "A Genre")
public class GenreView {
    @NotNull
    private String genre;
}