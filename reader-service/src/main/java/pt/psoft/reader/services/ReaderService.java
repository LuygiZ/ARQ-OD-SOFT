package pt.psoft.reader.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.reader.messaging.ReaderEventPublisher;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.repositories.ReaderRepository;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReaderService {

    private final ReaderRepository readerRepository;
    private final ReaderEventPublisher eventPublisher;

    @Transactional
    public Reader createReader(ReaderRequest request) {
        log.info("Creating reader for username: {}", request.getUsername());

        // 1. Validate if exists
        if (readerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 2. Create and Save Reader (State: PENDING - implicitly by not having User yet)
        Reader reader = new Reader(
                request.getUsername(),
                request.getFullName(),
                request.getBirthDate(),
                request.getPhoneNumber(),
                request.isGdprConsent()
        );
        reader = readerRepository.save(reader);

        // 3. Publish Event to trigger User creation (Saga Step 1)
        ReaderCreatedEvent event = new ReaderCreatedEvent(
                request.getUsername(),
                request.getPassword(),
                request.getFullName()
        );
        eventPublisher.publishReaderCreated(event);

        return reader;
    }

    public java.util.Optional<Reader> findByUsername(String username) {
        return readerRepository.findByUsername(username);
    }
}
