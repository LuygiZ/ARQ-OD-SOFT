package pt.psoft.lending.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Lending Service
 */
@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "lms.events";

    // Queue for this service
    public static final String QUEUE_NAME = "lending-service.events";

    // Routing keys - Publishing
    public static final String ROUTING_KEY_LENDING_CREATED = "lending.lending.created";
    public static final String ROUTING_KEY_LENDING_RETURNED = "lending.lending.returned";

    // Routing keys - Consuming (from other services)
    public static final String ROUTING_KEY_BOOK_ALL = "catalog.book.*";
    public static final String ROUTING_KEY_READER_ALL = "user.reader.*";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue lendingQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .build();
    }

    // Bindings for consuming book events
    @Bean
    public Binding bindingBookEvents(Queue lendingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(lendingQueue)
                .to(exchange)
                .with(ROUTING_KEY_BOOK_ALL);
    }

    // Bindings for consuming reader events
    @Bean
    public Binding bindingReaderEvents(Queue lendingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(lendingQueue)
                .to(exchange)
                .with(ROUTING_KEY_READER_ALL);
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
