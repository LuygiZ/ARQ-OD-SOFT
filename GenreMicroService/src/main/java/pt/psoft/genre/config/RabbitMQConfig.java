package pt.psoft.genre.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange para Domain Events
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange("lms.events", true, false);
    }

    // Queue para este serviço receber eventos (se necessário)
    @Bean
    public Queue genreEventsQueue() {
        return new Queue("genre-service.events", true);
    }

    // Binding (subscribe a eventos de interesse)
    @Bean
    public Binding genreEventsBinding(Queue genreEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(genreEventsQueue)
                .to(eventsExchange)
                .with("catalog.genre.#");
    }

    // Configurar RabbitTemplate com JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}