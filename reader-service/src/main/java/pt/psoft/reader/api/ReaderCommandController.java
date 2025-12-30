package pt.psoft.reader.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.services.ReaderRequest;
import pt.psoft.reader.services.ReaderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderCommandController {

    private final ReaderService readerService;

    @PostMapping
    public ResponseEntity<Reader> createReader(@Valid @RequestBody ReaderRequest request) {
        Reader created = readerService.createReader(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
