package pt.psoft.saga.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.saga.dto.CreateBookSagaRequest;
import pt.psoft.saga.dto.CreateBookSagaResponse;
import pt.psoft.saga.service.SagaOrchestrator;

/**
 * Catalog Controller - Orchestrates Book creation with Author and Genre
 *
 * Provides a single endpoint for librarians to create a complete Book entry
 */
@Tag(name = "Catalog", description = "Endpoints for managing the catalog (Books, Authors, Genres)")
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final SagaOrchestrator sagaOrchestrator;

    /**
     * Create a Book with Author and Genre (Saga Orchestration)
     *
     * This endpoint coordinates the creation of:
     * 1. Genre (if doesn't exist)
     * 2. Author (if doesn't exist)
     * 3. Book (with references to Genre and Author)
     *
     * Uses Saga Pattern for distributed transaction management
     * Automatically rolls back on failures
     */
    @Operation(
            summary = "Create Book with Author and Genre",
            description = "Creates a complete book entry including genre and author using Saga orchestration. " +
                    "Automatically handles rollback if any step fails."
    )
    @PostMapping("/books")
    public ResponseEntity<CreateBookSagaResponse> createBook(
            @Valid @RequestBody CreateBookSagaRequest request) {

        log.info("📚 Received request to create book: {}", request.getBook().getTitle());

        try {
            CreateBookSagaResponse response = sagaOrchestrator.createBook(request);

            log.info("✅ Book created successfully via Saga: {}", response.getSagaId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {
            log.error("❌ Failed to create book", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CreateBookSagaResponse.builder()
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * Get Saga status
     */
    @Operation(
            summary = "Get Saga status",
            description = "Retrieve the current status and details of a saga execution"
    )
    @GetMapping("/sagas/{sagaId}")
    public ResponseEntity<CreateBookSagaResponse> getSagaStatus(
            @PathVariable @Parameter(description = "Saga ID") String sagaId) {

        log.info("🔍 Getting status for Saga: {}", sagaId);

        try {
            CreateBookSagaResponse response = sagaOrchestrator.getSagaStatus(sagaId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to get saga status: {}", sagaId, e);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CreateBookSagaResponse.builder()
                            .errorMessage("Saga not found: " + sagaId)
                            .build());
        }
    }
}