package pt.psoft.book.cdc;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pt.psoft.shared.events.lending.LendingReturnedEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CDC Consumer Pact Test - Book Service consumes LendingReturnedEvent from Lending Service
 *
 * This test defines the contract that Book Service expects when consuming
 * LendingReturnedEvent messages from the Lending Service.
 *
 * Student C functionality: Reviews are published when a lending is returned
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "lending-service", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class LendingReturnedEventConsumerPactTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Pact(provider = "lending-service", consumer = "book-service")
    MessagePact lendingReturnedWithReviewPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringType("@type", "LendingReturned")
                .stringType("lendingNumber", "2025/1")
                .stringType("bookId", "978-0-13-468599-1")
                .integerType("readerId", 1L)
                .stringType("readerNumber", "2025/100")
                .stringType("returnDate", "2025-01-15")
                .stringType("comment", "Great book, highly recommended!")
                .integerType("rating", 8)
                .integerType("daysOverdue", 0)
                .integerType("fineAmount", 0);

        return builder
                .expectsToReceive("a lending returned event with review")
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "lending-service", consumer = "book-service")
    MessagePact lendingReturnedWithoutReviewPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringType("@type", "LendingReturned")
                .stringType("lendingNumber", "2025/2")
                .stringType("bookId", "978-0-13-468599-1")
                .integerType("readerId", 2L)
                .stringType("readerNumber", "2025/101")
                .stringType("returnDate", "2025-01-16")
                .nullValue("comment")
                .nullValue("rating")
                .integerType("daysOverdue", 0)
                .integerType("fineAmount", 0);

        return builder
                .expectsToReceive("a lending returned event without review")
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "lending-service", consumer = "book-service")
    MessagePact lendingReturnedOverduePact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringType("@type", "LendingReturned")
                .stringType("lendingNumber", "2025/3")
                .stringType("bookId", "978-0-13-468599-1")
                .integerType("readerId", 3L)
                .stringType("readerNumber", "2025/102")
                .stringType("returnDate", "2025-01-20")
                .stringType("comment", "Late return but worth it")
                .integerType("rating", 7)
                .integerType("daysOverdue", 5)
                .integerType("fineAmount", 500);

        return builder
                .expectsToReceive("a lending returned event with overdue fine")
                .withContent(body)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "lendingReturnedWithReviewPact")
    void testConsumeWithReview(List<Message> messages) throws Exception {
        assertThat(messages).hasSize(1);

        String json = messages.get(0).contentsAsString();
        LendingReturnedEvent event = objectMapper.readValue(json, LendingReturnedEvent.class);

        assertThat(event.getLendingNumber()).isNotBlank();
        assertThat(event.getBookId()).isNotBlank();
        assertThat(event.getReaderNumber()).isNotBlank();
        assertThat(event.getComment()).isNotBlank();
        assertThat(event.getRating()).isNotNull();
    }

    @Test
    @PactTestFor(pactMethod = "lendingReturnedWithoutReviewPact")
    void testConsumeWithoutReview(List<Message> messages) throws Exception {
        assertThat(messages).hasSize(1);

        String json = messages.get(0).contentsAsString();
        LendingReturnedEvent event = objectMapper.readValue(json, LendingReturnedEvent.class);

        assertThat(event.getLendingNumber()).isNotBlank();
        assertThat(event.getBookId()).isNotBlank();
    }

    @Test
    @PactTestFor(pactMethod = "lendingReturnedOverduePact")
    void testConsumeOverdue(List<Message> messages) throws Exception {
        assertThat(messages).hasSize(1);

        String json = messages.get(0).contentsAsString();
        LendingReturnedEvent event = objectMapper.readValue(json, LendingReturnedEvent.class);

        assertThat(event.getLendingNumber()).isNotBlank();
        assertThat(event.getDaysOverdue()).isNotNull();
        assertThat(event.getRating()).isNotNull();
    }
}
