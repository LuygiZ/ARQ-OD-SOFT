package pt.psoft.saga.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.psoft.shared.dto.book.BookDTO;
import pt.psoft.shared.dto.book.CreateBookRequest;

/**
 * Feign Client for Book Service
 * Uses shared-kernel DTOs
 */
@FeignClient(
        name = "book-command-service",
        url = "${feign.client.config.book-command-service.url}"
)
public interface BookServiceClient {

    @PostMapping("/api/books")
    @CircuitBreaker(name = "bookService", fallbackMethod = "createBookFallback")
    @Retry(name = "bookService")
    BookDTO createBook(@RequestBody CreateBookRequest request);

    @DeleteMapping("/api/books/{isbn}")
    @CircuitBreaker(name = "bookService")
    @Retry(name = "bookService")
    void deleteBook(@PathVariable("isbn") String isbn);

    @GetMapping("/api/books/{isbn}")
    @CircuitBreaker(name = "bookService")
    BookDTO getBook(@PathVariable("isbn") String isbn);

    // Fallback method
    default BookDTO createBookFallback(CreateBookRequest request, Exception e) {
        throw new RuntimeException("Book Service unavailable: " + e.getMessage(), e);
    }
}