package pt.psoft.user.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.psoft.user.repositories.jpa.OutboxRepository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.events.user.UserCreatedEvent; // Assuming this event exists

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publishUserCreated(UserCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            OutboxEvent outboxEvent = new OutboxEvent(
                    "USER",
                    event.getUsername(), 
                    "CREATED",
                    payload
            );

            outboxRepository.save(outboxEvent);
            log.info("Persisted UserCreatedEvent to Outbox: {}", event.getUsername());

        } catch (Exception e) {
            log.error("Failed to persist UserCreatedEvent to Outbox", e);
            throw new RuntimeException("Failed to persist event", e); 
        }
    }
}
