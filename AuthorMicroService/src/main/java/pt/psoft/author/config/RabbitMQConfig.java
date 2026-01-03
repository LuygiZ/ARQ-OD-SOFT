package pt.psoft.author.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Author Service
 */
@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "lms.events";

    // Queue for this service
    public static final String QUEUE_NAME = "author-service.events";

    // Routing keys
    public static final String ROUTING_KEY_AUTHOR_CREATED = "catalog.author.created";
    public static final String ROUTING_KEY_AUTHOR_UPDATED = "catalog.author.updated";
    public static final String ROUTING_KEY_AUTHOR_DELETED = "catalog.author.deleted";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue authorQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .build();
    }

    @Bean
    public Binding bindingAuthorCreated(Queue authorQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authorQueue)
                .to(exchange)
                .with(ROUTING_KEY_AUTHOR_CREATED);
    }

    @Bean
    public Binding bindingAuthorUpdated(Queue authorQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authorQueue)
                .to(exchange)
                .with(ROUTING_KEY_AUTHOR_UPDATED);
    }

    @Bean
    public Binding bindingAuthorDeleted(Queue authorQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authorQueue)
                .to(exchange)
                .with(ROUTING_KEY_AUTHOR_DELETED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}