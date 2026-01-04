package pt.psoft.reader.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.psoft.reader.external.ApiNinjasService;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.services.ReaderService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderQueryController {

    private final ReaderService readerService;
    private final ApiNinjasService apiNinjasService;

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getReader(@PathVariable String username) {
        Optional<Reader> readerOpt = readerService.findByUsername(username);

        if (readerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reader reader = readerOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("username", reader.getUsername());
        response.put("name", reader.getFullName());
        response.put("birthDate", reader.getBirthDate());

        if (reader.getBirthDate() != null) {
            LocalDate date = reader.getBirthDate();
            String event = apiNinjasService.getRandomEventFromYearMonth(date.getYear(), date.getMonthValue());
            response.put("historicalEvent", event);
        }

        return ResponseEntity.ok(response);
    }
}
