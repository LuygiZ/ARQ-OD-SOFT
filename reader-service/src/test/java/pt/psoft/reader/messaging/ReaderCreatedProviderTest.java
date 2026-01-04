package pt.psoft.reader.messaging;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;

import java.time.LocalDate;

@Provider("reader-service")
@PactFolder("../user-service/target/pacts") // Point to where Consumer generates the pact
public class ReaderCreatedProviderTest {

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void testTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @PactVerifyProvider("A Reader Created Event")
    public String verifyReaderCreatedEvent() throws JsonProcessingException {
        // Here we create an instance of the EVENT that the service ACTUALLY produces
        ReaderCreatedEvent event = new ReaderCreatedEvent(
            123L,
            "pact_reader@mail.com",
            "password123",
            "Pact Reader",
            "999888777",
            LocalDate.of(1999, 9, 9),
            true,
            false,
            null,
            null
        );
        
        // Serialize it exactly as the production code would
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper.writeValueAsString(event);
    }
}
