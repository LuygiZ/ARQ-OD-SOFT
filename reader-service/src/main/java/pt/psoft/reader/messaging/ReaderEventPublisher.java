package pt.psoft.reader.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pt.psoft.shared.events.DomainEvent;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReaderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishReaderCreated(ReaderCreatedEvent event) {
        log.info("Publishing ReaderCreatedEvent to RabbitMQ: {}", event.getUsername());
        // Exchange name, Routing Key, Event
        rabbitTemplate.convertAndSend("internal.exchange", "reader.created", event);
    }
}
