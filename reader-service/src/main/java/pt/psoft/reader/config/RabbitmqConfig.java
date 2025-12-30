package pt.psoft.reader.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String EXCHANGE_NAME = "internal.exchange";
    public static final String READER_CREATED_QUEUE = "reader.created.queue";
    public static final String USER_CREATED_QUEUE = "user.created.queue";

    @Bean
    public TopicExchange internalExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue readerCreatedQueue() {
        return new Queue(READER_CREATED_QUEUE);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE);
    }

    @Bean
    public Binding bindingReaderCreated() {
        return BindingBuilder.bind(readerCreatedQueue())
                .to(internalExchange())
                .with("reader.created");
    }

    @Bean
    public Binding bindingUserCreated() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(internalExchange())
                .with("user.created");
    }
}
