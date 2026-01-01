package pt.psoft.saga.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.psoft.shared.dto.author.AuthorDTO;
import pt.psoft.shared.dto.author.CreateAuthorRequest;

import java.util.List;

/**
 * Feign Client for Author Service
 * Uses shared-kernel DTOs
 */
@FeignClient(
        name = "author-service",
        url = "${feign.client.config.author-service.url}"
)
public interface AuthorServiceClient {

    @PostMapping("/api/authors")
    @CircuitBreaker(name = "authorService", fallbackMethod = "createAuthorFallback")
    @Retry(name = "authorService")
    AuthorDTO createAuthor(@RequestBody CreateAuthorRequest request);

    @DeleteMapping("/api/authors/{authorNumber}")
    @CircuitBreaker(name = "authorService")
    @Retry(name = "authorService")
    void deleteAuthor(@PathVariable("authorNumber") Long authorNumber);

    @GetMapping("/api/authors/{authorNumber}")
    @CircuitBreaker(name = "authorService")
    AuthorDTO getAuthor(@PathVariable("authorNumber") Long authorNumber);

    @GetMapping("/api/authors")
    @CircuitBreaker(name = "authorService")
    List<AuthorDTO> findByName(@RequestParam(value = "name", required = false) String name);

    // Fallback method
    default AuthorDTO createAuthorFallback(CreateAuthorRequest request, Exception e) {
        throw new RuntimeException("Author Service unavailable: " + e.getMessage(), e);
    }
}