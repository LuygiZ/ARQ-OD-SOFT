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

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private pt.psoft.reader.repositories.OutboxRepository outboxRepository;

    @InjectMocks
    private ReaderEventPublisher publisher;

    @Test
    void publishReaderCreated_ShouldSendCorrectMessage() throws Exception {
        // Arrange
        ReaderCreatedEvent event = new ReaderCreatedEvent("user1", "pass1", "John Doe");
        String dummyJson = "{\"username\":\"user1\"}";

        // Mock ObjectMapper behavior
        org.mockito.Mockito.when(objectMapper.writeValueAsString(eq(event))).thenReturn(dummyJson);

        // Act
        publisher.publishReaderCreated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq("internal.exchange"),
                eq("reader.created"),
                eq(event));
    }
}
