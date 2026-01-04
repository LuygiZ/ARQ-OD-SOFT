package pt.psoft.user.messaging;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "reader-service")
public class ReaderCreatedConsumerTest {

    @Pact(consumer = "user-service")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("username", "pact_reader@mail.com");
        body.stringType("password", "password123");
        body.stringType("fullName", "Pact Reader");

        return builder
                .expectsToReceive("A Reader Created Event")
                .withContent(body)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact")
    public void testReceiveReaderCreatedEvent(List<Message> messages) {
        assertEquals(1, messages.size());
        Message message = messages.get(0);

        // Verify that we can deserialize the message into our event class
        assertDoesNotThrow(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            // We ignore unknown properties because the actual event has @type, timestamp etc which we might not define in the minimal contract
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            String json = message.contentsAsString();
            ReaderCreatedEvent event = objectMapper.readValue(json, ReaderCreatedEvent.class);
            
            assertEquals("pact_reader@mail.com", event.getUsername());
        });
    }
}
