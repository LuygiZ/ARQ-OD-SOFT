package pt.psoft.author.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;

/**
 * Consumes Author events from RabbitMQ and publishes them as Spring local events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorEventConsumer {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @RabbitListener(queues = "author-service.events")
    @Transactional
    public void handleAuthorEvent(String message) {
        try {
            log.info("Received message from RabbitMQ: {}", message);

            JsonNode rootNode = objectMapper.readTree(message);
            String eventType = rootNode.get("@type").asText();

            switch (eventType) {
                case "AuthorCreated" -> {
                    AuthorCreatedEvent event = objectMapper.readValue(message, AuthorCreatedEvent.class);
                    log.info("Publishing local AuthorCreated event for number: {}", event.getAuthorNumber());
                    eventPublisher.publishEvent(event);
                }
                case "AuthorUpdated" -> {
                    AuthorUpdatedEvent event = objectMapper.readValue(message, AuthorUpdatedEvent.class);
                    log.info("Publishing local AuthorUpdated event for number: {}", event.getAuthorNumber());
                    eventPublisher.publishEvent(event);
                }
                case "AuthorDeleted" -> {
                    AuthorDeletedEvent event = objectMapper.readValue(message, AuthorDeletedEvent.class);
                    log.info("Publishing local AuthorDeleted event for number: {}", event.getAuthorNumber());
                    eventPublisher.publishEvent(event);
                }
                default -> log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing Author event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Author event", e);
        }
    }
}