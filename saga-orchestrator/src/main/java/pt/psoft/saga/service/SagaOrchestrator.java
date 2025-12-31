package pt.psoft.saga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Saga Orchestrator
 * Supports:
 * - Multiple NEW authors creation
 * - Using EXISTING author IDs
 * - Combination of both (new + existing)
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

    public CreateBookSagaResponse createBook(CreateBookSagaRequest request) {
        log.info("🎯 Starting Saga for Book creation: {}", request.getBook().getTitle());

        validateRequest(request);

        SagaInstance saga = createSagaInstance(request);
        saga = sagaRepository.save(saga);

        try {
            saga = executeGenreCreation(saga, request.getGenre());
            saga = executeAuthorsCreation(saga, request.getNewAuthors());
            saga = executeBookCreation(saga, request.getBook(), request.getExistingAuthorIds());

            saga.complete();
            saga = sagaRepository.save(saga);

            log.info("✅ Saga completed successfully: {}", saga.getSagaId());
            return buildSuccessResponse(saga);

        } catch (Exception e) {
            log.error("❌ Saga failed: {}. Starting compensation...", saga.getSagaId(), e);
            saga.fail(e.getMessage());
            saga = sagaRepository.save(saga);
            compensate(saga);
            throw new RuntimeException("Saga failed: " + e.getMessage(), e);
        }
    }

    private void validateRequest(CreateBookSagaRequest request) {
        boolean hasNewAuthors = request.getNewAuthors() != null && !request.getNewAuthors().isEmpty();
        boolean hasExistingAuthors = request.getExistingAuthorIds() != null && !request.getExistingAuthorIds().isEmpty();

        if (!hasNewAuthors && !hasExistingAuthors) {
            throw new IllegalArgumentException("Must provide either newAuthors or existingAuthorIds (or both)");
        }
    }

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
     * ✅ NEW: Create MULTIPLE authors if provided
     */
    private SagaInstance executeAuthorsCreation(SagaInstance saga, List<CreateBookSagaRequest.AuthorData> newAuthors) {
        if (newAuthors == null || newAuthors.isEmpty()) {
            log.info("📝 [STEP 2] No new authors to create - skipping");
            return saga;
        }

        log.info("📝 [STEP 2] Creating {} new author(s)", newAuthors.size());
        saga.setState(SagaState.CREATING_AUTHOR);
        saga = sagaRepository.save(saga);

        List<Long> createdAuthorNumbers = new ArrayList<>();
        List<AuthorDTO> createdAuthors = new ArrayList<>();

        try {
            for (int i = 0; i < newAuthors.size(); i++) {
                CreateBookSagaRequest.AuthorData authorData = newAuthors.get(i);
                log.info("📝 [STEP 2.{}] Creating Author: {}", i + 1, authorData.getName());

                CreateAuthorRequest authorRequest = new CreateAuthorRequest(
                        authorData.getName(),
                        authorData.getBio(),
                        authorData.getPhotoURI()
                );

                AuthorDTO authorResponse = authorServiceClient.createAuthor(authorRequest);
                createdAuthorNumbers.add(authorResponse.getAuthorNumber());
                createdAuthors.add(authorResponse);

                saga.addStep(SagaStep.success(
                        "CREATE_AUTHOR_" + (i + 1),
                        "author-service",
                        "CREATE",
                        toJson(authorResponse)
                ));

                log.info("✅ [STEP 2.{}] Author created: authorNumber={}", i + 1, authorResponse.getAuthorNumber());
            }

            // Store all created author numbers and responses
            saga.setAuthorNumber(createdAuthorNumbers.get(0)); // Keep first for backward compatibility
            saga.setAuthorResponse(toJson(createdAuthors));
            saga.setState(SagaState.AUTHOR_CREATED);
            saga = sagaRepository.save(saga);

            log.info("✅ [STEP 2] All {} authors created: {}", createdAuthorNumbers.size(), createdAuthorNumbers);
            return saga;

        } catch (Exception e) {
            log.error("❌ [STEP 2] Authors creation failed", e);
            saga.setState(SagaState.AUTHOR_CREATION_FAILED);
            saga.addStep(SagaStep.failure("CREATE_AUTHORS", "author-service", "CREATE", e.getMessage()));

            // Store partially created authors for compensation
            if (!createdAuthorNumbers.isEmpty()) {
                saga.setAuthorNumber(createdAuthorNumbers.get(0));
                saga.setAuthorResponse(toJson(createdAuthors));
            }

            sagaRepository.save(saga);
            throw new RuntimeException("Failed to create authors: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ ENHANCED: Create book with BOTH new and existing author IDs
     */
    private SagaInstance executeBookCreation(
            SagaInstance saga,
            CreateBookSagaRequest.BookData bookData,
            List<Long> existingAuthorIds) {

        log.info("📝 [STEP 3] Creating Book: {}", bookData.getTitle());
        saga.setState(SagaState.CREATING_BOOK);
        saga = sagaRepository.save(saga);

        try {
            // Gather ALL author IDs (new + existing)
            List<Long> allAuthorIds = new ArrayList<>();
            List<String> allAuthorNames = new ArrayList<>();

            // Add newly created authors
            if (saga.getAuthorResponse() != null) {
                List<AuthorDTO> createdAuthors = objectMapper.readValue(
                        saga.getAuthorResponse(),
                        new TypeReference<List<AuthorDTO>>() {}
                );

                for (AuthorDTO author : createdAuthors) {
                    allAuthorIds.add(author.getAuthorNumber());
                    allAuthorNames.add(author.getName());
                }

                log.info("📚 New authors: IDs={}, Names={}", allAuthorIds, allAuthorNames);
            }

            // Add existing authors
            if (existingAuthorIds != null && !existingAuthorIds.isEmpty()) {
                for (Long authorId : existingAuthorIds) {
                    AuthorDTO existingAuthor = authorServiceClient.getAuthor(authorId);
                    allAuthorIds.add(authorId);
                    allAuthorNames.add(existingAuthor.getName()); // real names
                }

                log.info("📚 Existing authors: IDs={}", existingAuthorIds);
            }

            log.info("📚 Total authors for book: IDs={}, Names={}", allAuthorIds, allAuthorNames);

            CreateBookRequest bookRequest = new CreateBookRequest(
                    bookData.getTitle(),
                    bookData.getDescription(),
                    bookData.getGenreName(),
                    allAuthorIds,
                    allAuthorNames,
                    bookData.getPhotoURI()
            );

            BookDTO bookResponse = bookServiceClient.createBook(bookRequest);

            log.info("✅ Received BookDTO: isbn={}, authorIds={}",
                    bookResponse.getIsbn(), bookResponse.getAuthorIds());

            saga.setBookId(bookResponse.getIsbn().hashCode() * 1L);
            saga.setBookResponse(toJson(bookResponse));
            saga.setState(SagaState.BOOK_CREATED);
            saga.addStep(SagaStep.success("CREATE_BOOK", "book-service", "CREATE", toJson(bookResponse)));
            saga = sagaRepository.save(saga);

            log.info("✅ [STEP 3] Book created: ISBN={} with {} authors",
                    bookResponse.getIsbn(), allAuthorIds.size());
            return saga;

        } catch (Exception e) {
            log.error("❌ [STEP 3] Book creation failed", e);
            saga.setState(SagaState.BOOK_CREATION_FAILED);
            saga.addStep(SagaStep.failure("CREATE_BOOK", "book-service", "CREATE", e.getMessage()));
            sagaRepository.save(saga);
            throw new RuntimeException("Failed to create book: " + e.getMessage(), e);
        }
    }

    private void compensate(SagaInstance saga) {
        log.warn("🔄 Starting compensation for Saga: {}", saga.getSagaId());
        saga.startCompensation();
        saga = sagaRepository.save(saga);

        try {
            // Compensate all created authors
            if (saga.getAuthorResponse() != null) {
                compensateAuthors(saga);
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
     * ✅ ENHANCED: Compensate ALL created authors
     */
    private void compensateAuthors(SagaInstance saga) {
        try {
            List<AuthorDTO> createdAuthors = objectMapper.readValue(
                    saga.getAuthorResponse(),
                    new TypeReference<List<AuthorDTO>>() {}
            );

            log.info("🔄 [COMPENSATE] Deleting {} author(s)", createdAuthors.size());

            for (int i = 0; i < createdAuthors.size(); i++) {
                AuthorDTO author = createdAuthors.get(i);
                try {
                    log.info("🔄 [COMPENSATE] Deleting Author {}/{}: authorNumber={}",
                            i + 1, createdAuthors.size(), author.getAuthorNumber());

                    authorServiceClient.deleteAuthor(author.getAuthorNumber());

                    saga.addStep(SagaStep.success(
                            "COMPENSATE_AUTHOR_" + (i + 1),
                            "author-service",
                            "DELETE",
                            "Author deleted: " + author.getAuthorNumber()
                    ));

                    log.info("✅ [COMPENSATE] Author deleted: authorNumber={}", author.getAuthorNumber());

                } catch (Exception e) {
                    log.error("❌ [COMPENSATE] Failed to delete Author: authorNumber={}",
                            author.getAuthorNumber(), e);
                    saga.addStep(SagaStep.failure(
                            "COMPENSATE_AUTHOR_" + (i + 1),
                            "author-service",
                            "DELETE",
                            e.getMessage()
                    ));
                    throw e;
                }
            }

        } catch (JsonProcessingException e) {
            log.error("❌ [COMPENSATE] Failed to parse authors for compensation", e);
            throw new RuntimeException("Failed to parse authors for compensation", e);
        }
    }

    public CreateBookSagaResponse getSagaStatus(String sagaId) {
        SagaInstance saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        return buildSuccessResponse(saga);
    }

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
            List<CreateBookSagaResponse.AuthorResponse> authorResps = new ArrayList<>();
            CreateBookSagaResponse.BookResponse bookResp = null;

            if (saga.getGenreResponse() != null) {
                GenreDTO gr = objectMapper.readValue(saga.getGenreResponse(), GenreDTO.class);
                genreResp = CreateBookSagaResponse.GenreResponse.builder()
                        .id(Long.parseLong(gr.getId()))
                        .name(gr.getGenre())
                        .build();
            }

            if (saga.getAuthorResponse() != null) {
                List<AuthorDTO> authors = objectMapper.readValue(
                        saga.getAuthorResponse(),
                        new TypeReference<List<AuthorDTO>>() {}
                );

                for (AuthorDTO ar : authors) {
                    authorResps.add(CreateBookSagaResponse.AuthorResponse.builder()
                            .authorNumber(ar.getAuthorNumber())
                            .name(ar.getName())
                            .bio(ar.getBio())
                            .photoURI(ar.getPhotoURI())
                            .build());
                }
            }

            if (saga.getBookResponse() != null) {
                BookDTO br = objectMapper.readValue(saga.getBookResponse(), BookDTO.class);

                bookResp = CreateBookSagaResponse.BookResponse.builder()
                        .isbn(br.getIsbn())
                        .title(br.getTitle())
                        .description(br.getDescription())
                        .genre(br.getGenre())
                        .authors(br.getAuthorIds() != null ? br.getAuthorIds() : List.of())
                        .build();
            }

            return CreateBookSagaResponse.builder()
                    .sagaId(saga.getSagaId())
                    .state(saga.getState())
                    .startedAt(saga.getStartedAt())
                    .completedAt(saga.getCompletedAt())
                    .genre(genreResp)
                    .authors(authorResps)  // ✅ Now returns LIST of authors
                    .book(bookResp)
                    .errorMessage(saga.getErrorMessage())
                    .build();

        } catch (Exception e) {
            log.error("❌ Failed to build response", e);
            throw new RuntimeException("Failed to build response", e);
        }
    }
}