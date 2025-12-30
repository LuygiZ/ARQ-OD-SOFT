package pt.psoft.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.psoft.saga.client.AuthorServiceClient;
import pt.psoft.saga.client.BookServiceClient;
import pt.psoft.saga.client.GenreServiceClient;
import pt.psoft.saga.dto.CreateBookSagaRequest;
import pt.psoft.saga.dto.CreateBookSagaResponse;
import pt.psoft.saga.model.SagaInstance;
import pt.psoft.saga.model.SagaState;
import pt.psoft.saga.model.SagaStep;
import pt.psoft.saga.repository.SagaRepository;
import pt.psoft.shared.dto.author.AuthorDTO;
import pt.psoft.shared.dto.author.CreateAuthorRequest;
import pt.psoft.shared.dto.book.BookDTO;
import pt.psoft.shared.dto.book.CreateBookRequest;
import pt.psoft.shared.dto.genre.CreateGenreRequest;
import pt.psoft.shared.dto.genre.GenreDTO;

import java.util.List;

/**
 * Saga Orchestrator - Coordinates distributed transactions
 *
 * Orchestrates the creation of Book + Author + Genre across 3 microservices
 * Implements compensation logic for rollback on failures
 * Uses shared-kernel DTOs for service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final SagaRepository sagaRepository;
    private final GenreServiceClient genreServiceClient;
    private final AuthorServiceClient authorServiceClient;
    private final BookServiceClient bookServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Execute Saga for Book creation
     */
    public CreateBookSagaResponse createBook(CreateBookSagaRequest request) {
        log.info("🎯 Starting Saga for Book creation: {}", request.getBook().getTitle());

        // Create and save saga instance
        SagaInstance saga = createSagaInstance(request);
        saga = sagaRepository.save(saga);

        try {
            // STEP 1: Create Genre
            saga = executeGenreCreation(saga, request.getGenre());

            // STEP 2: Create Author
            saga = executeAuthorCreation(saga, request.getAuthor());

            // STEP 3: Create Book
            saga = executeBookCreation(saga, request.getBook(), saga.getGenreId());

            // Complete saga
            saga.complete();
            saga = sagaRepository.save(saga);

            log.info("✅ Saga completed successfully: {}", saga.getSagaId());
            return buildSuccessResponse(saga);

        } catch (Exception e) {
            log.error("❌ Saga failed: {}. Starting compensation...", saga.getSagaId(), e);
            saga.fail(e.getMessage());
            saga = sagaRepository.save(saga);

            // Execute compensation
            compensate(saga);

            throw new RuntimeException("Saga failed: " + e.getMessage(), e);
        }
    }

    /**
     * STEP 1: Create Genre
     */
    private SagaInstance executeGenreCreation(SagaInstance saga, CreateBookSagaRequest.GenreData genreData) {
        log.info("📝 [STEP 1] Creating Genre: {}", genreData.getName());

        saga.setState(SagaState.CREATING_GENRE);
        saga = sagaRepository.save(saga);

        try {
            CreateGenreRequest genreRequest = new CreateGenreRequest(genreData.getName());
            GenreDTO genreResponse = genreServiceClient.createGenre(genreRequest);

            saga.setGenreId(Long.parseLong(genreResponse.getId()));
            saga.setGenreResponse(toJson(genreResponse));
            saga.setState(SagaState.GENRE_CREATED);
            saga.addStep(SagaStep.success("CREATE_GENRE", "genre-service", "CREATE", toJson(genreResponse)));
            saga = sagaRepository.save(saga);

            log.info("✅ [STEP 1] Genre created: ID={}", genreResponse.getId());
            return saga;

        } catch (Exception e) {
            log.error("❌ [STEP 1] Genre creation failed", e);
            saga.setState(SagaState.GENRE_CREATION_FAILED);
            saga.addStep(SagaStep.failure("CREATE_GENRE", "genre-service", "CREATE", e.getMessage()));
            sagaRepository.save(saga);
            throw new RuntimeException("Failed to create genre: " + e.getMessage(), e);
        }
    }

    /**
     * STEP 2: Create Author
     */
    private SagaInstance executeAuthorCreation(SagaInstance saga, CreateBookSagaRequest.AuthorData authorData) {
        log.info("📝 [STEP 2] Creating Author: {}", authorData.getName());

        saga.setState(SagaState.CREATING_AUTHOR);
        saga = sagaRepository.save(saga);

        try {
            CreateAuthorRequest authorRequest = new CreateAuthorRequest(
                    authorData.getName(),
                    authorData.getBio(),
                    authorData.getPhotoURI()
            );

            AuthorDTO authorResponse = authorServiceClient.createAuthor(authorRequest);

            saga.setAuthorNumber(authorResponse.getAuthorNumber());
            saga.setAuthorResponse(toJson(authorResponse));
            saga.setState(SagaState.AUTHOR_CREATED);
            saga.addStep(SagaStep.success("CREATE_AUTHOR", "author-service", "CREATE", toJson(authorResponse)));
            saga = sagaRepository.save(saga);

            log.info("✅ [STEP 2] Author created: authorNumber={}", authorResponse.getAuthorNumber());
            return saga;

        } catch (Exception e) {
            log.error("❌ [STEP 2] Author creation failed", e);
            saga.setState(SagaState.AUTHOR_CREATION_FAILED);
            saga.addStep(SagaStep.failure("CREATE_AUTHOR", "author-service", "CREATE", e.getMessage()));
            sagaRepository.save(saga);
            throw new RuntimeException("Failed to create author: " + e.getMessage(), e);
        }
    }

    /**
     * STEP 3: Create Book
     */
    private SagaInstance executeBookCreation(SagaInstance saga, CreateBookSagaRequest.BookData bookData, Long genreId) {
        log.info("📝 [STEP 3] Creating Book: {}", bookData.getTitle());

        saga.setState(SagaState.CREATING_BOOK);
        saga = sagaRepository.save(saga);

        try {
            // Note: Genre is passed by NAME in CreateBookRequest (from uploaded file)
            // But we need to map genreId to genre name - for now using the original genre name
            CreateBookRequest bookRequest = new CreateBookRequest(
                    bookData.getTitle(),
                    bookData.getDescription(),
                    bookData.getGenreName(), // Use genre name from request
                    List.of(saga.getAuthorNumber()), // Single author for now
                    bookData.getPhotoURI()
            );

            BookDTO bookResponse = bookServiceClient.createBook(bookRequest);

            saga.setBookId(bookResponse.getIsbn().hashCode() * 1L); // Temporary ID since Book uses ISBN
            saga.setBookResponse(toJson(bookResponse));
            saga.setState(SagaState.BOOK_CREATED);
            saga.addStep(SagaStep.success("CREATE_BOOK", "book-service", "CREATE", toJson(bookResponse)));
            saga = sagaRepository.save(saga);

            log.info("✅ [STEP 3] Book created: ISBN={}", bookResponse.getIsbn());
            return saga;

        } catch (Exception e) {
            log.error("❌ [STEP 3] Book creation failed", e);
            saga.setState(SagaState.BOOK_CREATION_FAILED);
            saga.addStep(SagaStep.failure("CREATE_BOOK", "book-service", "CREATE", e.getMessage()));
            sagaRepository.save(saga);
            throw new RuntimeException("Failed to create book: " + e.getMessage(), e);
        }
    }

    /**
     * Compensation logic - Rollback in reverse order
     */
    private void compensate(SagaInstance saga) {
        log.warn("🔄 Starting compensation for Saga: {}", saga.getSagaId());

        saga.startCompensation();
        saga = sagaRepository.save(saga);

        try {
            // Compensate in REVERSE order: Book → Author → Genre

            // Only compensate if the step succeeded
            if (saga.getAuthorNumber() != null) {
                compensateAuthor(saga);
            }

            if (saga.getGenreId() != null) {
                compensateGenre(saga);
            }

            saga.compensated();
            saga = sagaRepository.save(saga);

            log.info("✅ Compensation completed for Saga: {}", saga.getSagaId());

        } catch (Exception e) {
            log.error("❌ Compensation failed for Saga: {}", saga.getSagaId(), e);
            saga.setState(SagaState.COMPENSATION_FAILED);
            saga.setErrorMessage("Compensation failed: " + e.getMessage());
            sagaRepository.save(saga);
        }
    }

    /**
     * Compensate Genre (DELETE)
     */
    private void compensateGenre(SagaInstance saga) {
        log.info("🔄 [COMPENSATE] Deleting Genre: ID={}", saga.getGenreId());

        try {
            genreServiceClient.deleteGenre(saga.getGenreId());
            saga.addStep(SagaStep.success("COMPENSATE_GENRE", "genre-service", "DELETE", "Genre deleted"));
            log.info("✅ [COMPENSATE] Genre deleted: ID={}", saga.getGenreId());
        } catch (Exception e) {
            log.error("❌ [COMPENSATE] Failed to delete Genre: ID={}", saga.getGenreId(), e);
            saga.addStep(SagaStep.failure("COMPENSATE_GENRE", "genre-service", "DELETE", e.getMessage()));
            throw e;
        }
    }

    /**
     * Compensate Author (DELETE)
     */
    private void compensateAuthor(SagaInstance saga) {
        log.info("🔄 [COMPENSATE] Deleting Author: authorNumber={}", saga.getAuthorNumber());

        try {
            authorServiceClient.deleteAuthor(saga.getAuthorNumber());
            saga.addStep(SagaStep.success("COMPENSATE_AUTHOR", "author-service", "DELETE", "Author deleted"));
            log.info("✅ [COMPENSATE] Author deleted: authorNumber={}", saga.getAuthorNumber());
        } catch (Exception e) {
            log.error("❌ [COMPENSATE] Failed to delete Author: authorNumber={}", saga.getAuthorNumber(), e);
            saga.addStep(SagaStep.failure("COMPENSATE_AUTHOR", "author-service", "DELETE", e.getMessage()));
            throw e;
        }
    }

    /**
     * Get Saga status
     */
    public CreateBookSagaResponse getSagaStatus(String sagaId) {
        SagaInstance saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        return buildSuccessResponse(saga);
    }

    // Helper methods

    private SagaInstance createSagaInstance(CreateBookSagaRequest request) {
        try {
            return SagaInstance.create(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    private CreateBookSagaResponse buildSuccessResponse(SagaInstance saga) {
        try {
            CreateBookSagaResponse.GenreResponse genreResp = null;
            CreateBookSagaResponse.AuthorResponse authorResp = null;
            CreateBookSagaResponse.BookResponse bookResp = null;

            if (saga.getGenreResponse() != null) {
                GenreDTO gr = objectMapper.readValue(saga.getGenreResponse(), GenreDTO.class);
                genreResp = CreateBookSagaResponse.GenreResponse.builder()
                        .id(Long.parseLong(gr.getId()))
                        .name(gr.getGenre())
                        .build();
            }

            if (saga.getAuthorResponse() != null) {
                AuthorDTO ar = objectMapper.readValue(saga.getAuthorResponse(), AuthorDTO.class);
                authorResp = CreateBookSagaResponse.AuthorResponse.builder()
                        .authorNumber(ar.getAuthorNumber())
                        .name(ar.getName())
                        .bio(ar.getBio())
                        .photoURI(ar.getPhotoURI())
                        .build();
            }

            if (saga.getBookResponse() != null) {
                BookDTO br = objectMapper.readValue(saga.getBookResponse(), BookDTO.class);
                bookResp = CreateBookSagaResponse.BookResponse.builder()
                        .isbn(br.getIsbn())
                        .title(br.getTitle())
                        .description(br.getDescription())
                        .genre(br.getGenre())
                        .authors(br.getAuthorIds())
                        .build();
            }

            return CreateBookSagaResponse.builder()
                    .sagaId(saga.getSagaId())
                    .state(saga.getState())
                    .startedAt(saga.getStartedAt())
                    .completedAt(saga.getCompletedAt())
                    .genre(genreResp)
                    .author(authorResp)
                    .book(bookResp)
                    .errorMessage(saga.getErrorMessage())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build response", e);
        }
    }
}