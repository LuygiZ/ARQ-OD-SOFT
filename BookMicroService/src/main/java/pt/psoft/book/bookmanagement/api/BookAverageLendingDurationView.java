package pt.psoft.book.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Book and its average lending duration")
public class BookAverageLendingDurationView {
    BookView book;
    Double averageLendingDuration;
}
