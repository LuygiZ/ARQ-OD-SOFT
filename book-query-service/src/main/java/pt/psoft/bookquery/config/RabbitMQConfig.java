package pt.psoft.bookquery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Book Query Service
 * Only consumes events (no publishing - that's the command side's job)
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "lms.events";
    public static final String QUEUE_NAME = "book-query-service.events";

    // Routing keys for Book events (consumed)
    public static final String ROUTING_KEY_CREATED = "catalog.book.created";
    public static final String ROUTING_KEY_UPDATED = "catalog.book.updated";
    public static final String ROUTING_KEY_DELETED = "catalog.book.deleted";
    public static final String ROUTING_KEY_RATING_UPDATED = "review.book.rating_updated";

    // Routing keys for Lending events (for reviews)
    public static final String ROUTING_KEY_LENDING_RETURNED = "lending.lending.returned";

    @Bean
    public TopicExchange lmsEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue bookQueryEventsQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .build();
    }

    @Bean
    public Binding bookCreatedBinding(Queue bookQueryEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookQueryEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bookUpdatedBinding(Queue bookQueryEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookQueryEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_UPDATED);
    }

    @Bean
    public Binding bookDeletedBinding(Queue bookQueryEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookQueryEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_DELETED);
    }

    @Bean
    public Binding bookRatingUpdatedBinding(Queue bookQueryEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookQueryEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_RATING_UPDATED);
    }

    @Bean
    public Binding lendingReturnedBinding(Queue bookQueryEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookQueryEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_LENDING_RETURNED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
