package pt.psoft.book.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Books with lending count")
public class BookCountView {
    @NotNull
    private BookView bookView;

    private Long lendingCount;
}
