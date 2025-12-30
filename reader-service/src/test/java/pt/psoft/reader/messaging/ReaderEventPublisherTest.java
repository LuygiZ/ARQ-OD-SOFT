package pt.psoft.reader.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReaderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReaderEventPublisher publisher;

    @Test
    void publishReaderCreated_ShouldSendCorrectMessage() {
        // Arrange
        ReaderCreatedEvent event = new ReaderCreatedEvent("user1", "pass1", "John Doe");

        // Act
        publisher.publishReaderCreated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq("internal.exchange"),
                eq("reader.created"),
                eq(event)
        );
    }
}
