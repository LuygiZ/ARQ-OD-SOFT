package pt.psoft.lending.cdc;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import pt.psoft.shared.events.lending.LendingReturnedEvent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * CDC Provider Pact Test - Lending Service provides LendingReturnedEvent to Book Service
 *
 * This test verifies that Lending Service produces messages that match
 * the contract expected by Book Service (the consumer).
 *
 * Student C functionality: Validates the review event contract
 */
@Provider("lending-service")
@Consumer("book-service")
@PactFolder("pacts")
class LendingReturnedEventProviderPactTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        if (context != null) {
            context.setTarget(new MessageTestTarget());
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @PactVerifyProvider("a lending returned event with review")
    MessageAndMetadata lendingReturnedWithReview() throws Exception {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/1");
        event.setBookId("978-0-13-468599-1");
        event.setReaderId(1L);
        event.setReaderNumber("2025/100");
        event.setReturnDate(LocalDate.of(2025, 1, 15));
        event.setComment("Great book, highly recommended!");
        event.setRating(8);
        event.setDaysOverdue(0);
        event.setFineAmountInCents(0);

        String json = objectMapper.writeValueAsString(event);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return new MessageAndMetadata(json.getBytes(), metadata);
    }

    @PactVerifyProvider("a lending returned event without review")
    MessageAndMetadata lendingReturnedWithoutReview() throws Exception {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/2");
        event.setBookId("978-0-13-468599-1");
        event.setReaderId(2L);
        event.setReaderNumber("2025/101");
        event.setReturnDate(LocalDate.of(2025, 1, 16));
        event.setComment(null);
        event.setRating(null);
        event.setDaysOverdue(0);
        event.setFineAmountInCents(0);

        String json = objectMapper.writeValueAsString(event);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return new MessageAndMetadata(json.getBytes(), metadata);
    }

    @PactVerifyProvider("a lending returned event with overdue fine")
    MessageAndMetadata lendingReturnedOverdue() throws Exception {
        LendingReturnedEvent event = new LendingReturnedEvent();
        event.setLendingNumber("2025/3");
        event.setBookId("978-0-13-468599-1");
        event.setReaderId(3L);
        event.setReaderNumber("2025/102");
        event.setReturnDate(LocalDate.of(2025, 1, 20));
        event.setComment("Late return but worth it");
        event.setRating(7);
        event.setDaysOverdue(5);
        event.setFineAmountInCents(500);

        String json = objectMapper.writeValueAsString(event);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return new MessageAndMetadata(json.getBytes(), metadata);
    }
}
