package pt.psoft.reader.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoricalEventsResponse {
    private String year;
    private String month;
    private String day;
    @Getter
    private String event;
}
