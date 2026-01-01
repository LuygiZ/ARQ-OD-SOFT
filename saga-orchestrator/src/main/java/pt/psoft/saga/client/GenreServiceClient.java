package pt.psoft.saga.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pt.psoft.shared.dto.genre.CreateGenreRequest;
import pt.psoft.shared.dto.genre.GenreDTO;

/**
 * Feign Client for Genre Service
 * Uses shared-kernel DTOs
 */
@FeignClient(
        name = "genre-service",
        url = "${feign.client.config.genre-service.url}"
)
public interface GenreServiceClient {

    @PostMapping("/api/genres")
    @CircuitBreaker(name = "genreService", fallbackMethod = "createGenreFallback")
    @Retry(name = "genreService")
    GenreDTO createGenre(@RequestBody CreateGenreRequest request);

    @DeleteMapping("/api/genres/{id}")
    @CircuitBreaker(name = "genreService")
    @Retry(name = "genreService")
    void deleteGenre(@PathVariable("id") Long id);

    @GetMapping("/api/genres/{id}")
    @CircuitBreaker(name = "genreService")
    GenreDTO getGenre(@PathVariable("id") Long id);

    // Fallback method
    default GenreDTO createGenreFallback(CreateGenreRequest request, Exception e) {
        throw new RuntimeException("Genre Service unavailable: " + e.getMessage(), e);
    }
}