package pt.psoft.book.shared.messaging;

import pt.psoft.shared.events.DomainEvent;

/**
 * Interface for publishing domain events
 * Implementations should handle RabbitMQ publishing
 */
public interface EventPublisher {

    /**
     * Publish domain event to message broker
     */
    void publish(DomainEvent event);

    /**
     * Publish domain event with specific routing key
     */
    void publish(DomainEvent event, String routingKey);
}