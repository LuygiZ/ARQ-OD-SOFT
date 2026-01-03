package pt.psoft.book.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventConsumer {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "book-service.events")
    @Transactional
    public void handleBookEvent(String message) {
        log.info("Received message from RabbitMQ: {}", message);

        try {
            var jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("@type").asText();

            switch (eventType) {
                case "BookCreated" -> {
                    BookCreatedEvent event = objectMapper.readValue(message, BookCreatedEvent.class);
                    log.info("Publishing local BookCreated event for ISBN: {}", event.getIsbn());
                    eventPublisher.publishEvent(event);
                }
                case "BookUpdated" -> {
                    BookUpdatedEvent event = objectMapper.readValue(message, BookUpdatedEvent.class);
                    log.info("Publishing local BookUpdated event for ISBN: {}", event.getIsbn());
                    eventPublisher.publishEvent(event);
                }
                case "BookDeleted" -> {
                    BookDeletedEvent event = objectMapper.readValue(message, BookDeletedEvent.class);
                    log.info("Publishing local BookDeleted event for ISBN: {}", event.getIsbn());
                    eventPublisher.publishEvent(event);
                }
                default -> log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
            throw new RuntimeException("Failed to process event", e);
        }
    }
}