package pt.psoft.book.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Book Service
 * Following genre-service pattern
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "lms.events";
    public static final String QUEUE_NAME = "book-service.events";

    // Routing keys
    public static final String ROUTING_KEY_CREATED = "catalog.book.created";
    public static final String ROUTING_KEY_UPDATED = "catalog.book.updated";
    public static final String ROUTING_KEY_DELETED = "catalog.book.deleted";

    @Bean
    public TopicExchange lmsEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue bookEventsQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .build();
    }

    @Bean
    public Binding bookCreatedBinding(Queue bookEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bookUpdatedBinding(Queue bookEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_UPDATED);
    }

    @Bean
    public Binding bookDeletedBinding(Queue bookEventsQueue, TopicExchange lmsEventsExchange) {
        return BindingBuilder.bind(bookEventsQueue)
                .to(lmsEventsExchange)
                .with(ROUTING_KEY_DELETED);
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