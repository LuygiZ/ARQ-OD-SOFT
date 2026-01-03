package pt.psoft.genre.genremanagement.services;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAverageLendingsQuery {
    private int year;

    @Min(value = 1)
    @Max(value = 12)
    private int month;
}
