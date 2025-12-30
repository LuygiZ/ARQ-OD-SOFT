package pt.psoft.user.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String EXCHANGE_NAME = "internal.exchange";
    public static final String READER_CREATED_QUEUE = "reader.created.queue";

    // We only need to define the queue we listen to, or the one we bind to if we were creating it.
    // Ideally, the queues are defined in common or created by the service that owns the data.
    // For safety, we can redefine them here to ensure they exist.

    @Bean
    public Queue readerCreatedQueue() {
        return new Queue(READER_CREATED_QUEUE);
    }
}
