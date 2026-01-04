package pt.psoft.saga.cdc;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
public class SagaServiceConsumerTest {

    /**
     * PACT CONTRACT: Saga Orchestrator → Genre Service
     *
     * Defines the contract for creating a genre during saga execution
     */
    @Pact(consumer = "SagaOrchestrator", provider = "GenreService")
    public V4Pact createGenrePact(PactDslWithProvider builder) {
        return builder
                .given("No genre with name 'Science Fiction' exists")
                .uponReceiving("A request to create a new genre")
                .path("/api/genres")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("genreName", "Science Fiction"))
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("genreName", "Science Fiction"))
                .toPact(V4Pact.class);
    }

    /**
     * PACT CONTRACT: Saga Orchestrator → Genre Service (Search)
     *
     * Defines the contract for searching existing genres
     */
    @Pact(consumer = "SagaOrchestrator", provider = "GenreService")
    public V4Pact searchGenrePact(PactDslWithProvider builder) {
        return builder
                .given("A genre with name 'Science Fiction' exists")
                .uponReceiving("A request to search for a genre by name")
                .path("/api/genres/search")
                .query("name=Science%20Fiction")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("genreName", "Science Fiction"))
                .toPact(V4Pact.class);
    }

    /**
     * PACT CONTRACT: Saga Orchestrator → Author Service
     *
     * Defines the contract for creating an author during saga execution
     */
    @Pact(consumer = "SagaOrchestrator", provider = "AuthorService")
    public V4Pact createAuthorPact(PactDslWithProvider builder) {
        return builder
                .given("No author with name 'Isaac Asimov' exists")
                .uponReceiving("A request to create a new author")
                .path("/api/authors")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("name", "Isaac Asimov")
                        .stringType("bio", "American science fiction author")
                        .stringType("photoURI", "https://example.com/asimov.jpg"))
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .integerType("authorNumber", 1)
                        .stringType("name", "Isaac Asimov")
                        .stringType("bio", "American science fiction author")
                        .stringType("photoURI", "https://example.com/asimov.jpg"))
                .toPact(V4Pact.class);
    }

    /**
     * PACT CONTRACT: Saga Orchestrator → Book Service
     *
     * Defines the contract for creating a book during saga execution
     *
     * ✅ CORRIGIDO: Array de authorIds agora usa eachLike() corretamente
     */
    @Pact(consumer = "SagaOrchestrator", provider = "BookService")
    public V4Pact createBookPact(PactDslWithProvider builder) {
        return builder
                .given("Genre and Author exist, no book with ISBN '978-0553293357' exists")
                .uponReceiving("A request to create a new book")
                .path("/api/books")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("isbn", "978-0553293357")
                        .stringType("title", "Foundation")
                        .stringType("description", "A classic science fiction novel")
                        .stringType("genre", "Science Fiction")
                        .array("authorIds")  // ✅ CORREÇÃO: Usar array() ao invés de minArrayLike()
                        .integerType(1)
                        .closeArray()
                        .asBody()
                        .stringType("photoURI", "https://example.com/foundation.jpg"))
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .stringType("isbn", "978-0553293357")
                        .stringType("title", "Foundation")
                        .stringType("description", "A classic science fiction novel")
                        .stringType("genre", "Science Fiction")
                        .stringType("photoURI", "https://example.com/foundation.jpg"))
                .toPact(V4Pact.class);
    }

    /**
     * TEST: Saga can create a genre via Genre Service
     *
     * Verifies the Saga Orchestrator can successfully call Genre Service
     * to create a new genre as part of the saga workflow.
     */
    @Test
    @PactTestFor(pactMethod = "createGenrePact", port = "8081")
    void testCreateGenre(MockServer mockServer) {
        // Arrange
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build();

        Map<String, String> request = new HashMap<>();
        request.put("genreName", "Science Fiction");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/genres",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("genreName")).isEqualTo("Science Fiction");
        assertThat(response.getBody().get("id")).isNotNull();
    }

    /**
     * TEST: Saga can search for existing genre
     *
     * Verifies the Saga Orchestrator can search for existing genres
     * to avoid duplicates before creating a new one.
     */
    @Test
    @PactTestFor(pactMethod = "searchGenrePact", port = "8081")
    void testSearchGenre(MockServer mockServer) {
        // Arrange
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build();

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/genres/search?name=Science%20Fiction",
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("genreName")).isEqualTo("Science Fiction");
    }

    /**
     * TEST: Saga can create an author via Author Service
     *
     * Verifies the Saga Orchestrator can successfully call Author Service
     * to create a new author as part of the saga workflow.
     */
    @Test
    @PactTestFor(pactMethod = "createAuthorPact", port = "8082")
    void testCreateAuthor(MockServer mockServer) {
        // Arrange
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build();

        Map<String, String> request = new HashMap<>();
        request.put("name", "Isaac Asimov");
        request.put("bio", "American science fiction author");
        request.put("photoURI", "https://example.com/asimov.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/authors",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Isaac Asimov");
        assertThat(response.getBody().get("authorNumber")).isNotNull();
    }

    /**
     * TEST: Saga can create a book via Book Service
     *
     * Verifies the Saga Orchestrator can successfully call Book Service
     * to create a new book after genre and author are created.
     */
    @Test
    @PactTestFor(pactMethod = "createBookPact", port = "8083")
    void testCreateBook(MockServer mockServer) {
        // Arrange
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build();

        Map<String, Object> request = new HashMap<>();
        request.put("isbn", "978-0553293357");
        request.put("title", "Foundation");
        request.put("description", "A classic science fiction novel");
        request.put("genre", "Science Fiction");
        request.put("authorIds", Collections.singletonList(1)); // ✅ Usar List ao invés de array
        request.put("photoURI", "https://example.com/foundation.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/books",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isbn")).isEqualTo("978-0553293357");
        assertThat(response.getBody().get("title")).isEqualTo("Foundation");
        assertThat(response.getBody().get("genre")).isEqualTo("Science Fiction");
    }
}