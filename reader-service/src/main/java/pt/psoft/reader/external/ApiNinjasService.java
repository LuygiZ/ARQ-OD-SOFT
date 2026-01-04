package pt.psoft.reader.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiNinjasService {
    private final WebClient webClient;

    private List<HistoricalEventsResponse> getHistoricalEventsFromYearMonth(int year, int month) {
        return webClient.get()
                .uri("historicalevents?year=" + year + "&month=" + month)
                .retrieve()
                .bodyToFlux(HistoricalEventsResponse.class)
                .collectList()
                .block();
    }

    public String getRandomEventFromYearMonth(int year, int month) {
        try {
            final var responseList = getHistoricalEventsFromYearMonth(year, month);
            if (responseList == null || responseList.isEmpty()) {
                return "No historical event found.";
            }
            int randomIndex = (int) (Math.random() * responseList.size());
            return responseList.get(randomIndex).getEvent();
        } catch (Exception e) {
            return "Could not fetch historical event.";
        }
    }
}
