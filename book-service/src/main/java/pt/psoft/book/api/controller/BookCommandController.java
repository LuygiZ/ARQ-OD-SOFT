package pt.psoft.book.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.book.api.BookView;
import pt.psoft.book.api.UpdateBookRequest;
import pt.psoft.book.model.command.BookEntity;
import pt.psoft.book.services.BookCommandService;
import pt.psoft.shared.dto.book.CreateBookRequest;
import pt.psoft.shared.utils.IsbnGenerator;

import java.util.List;

/**
 * ✅ CORRECTED: Book Command Controller
 * BookView.authors now returns author IDs (List<Long>) instead of names
 * This allows proper Feign Client mapping to BookDTO.authorIds
 */
@Tag(name = "Books - Commands", description = "Endpoints for book write operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Slf4j
public class BookCommandController {

    private final BookCommandService bookCommandService;

    @Operation(summary = "Create a new book (auto-generate ISBN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookView> createBookAutoISBN(
            @Valid @RequestBody CreateBookRequest request) {

        log.info("Creating book with authorIds={}, authorNames={}",
                request.getAuthorIds(), request.getAuthorNames());

        String isbn = IsbnGenerator.generateValidIsbn();
        BookEntity book = bookCommandService.createBook(isbn, request);

        var newBookUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{isbn}")
                .buildAndExpand(book.getIsbnValue())
                .toUri();

        BookView view = mapToBookView(book);

        log.info("Returning BookView: isbn={}, authors={}", view.getIsbn(), view.getAuthors());

        return ResponseEntity.created(newBookUri)
                .eTag(String.valueOf(book.getVersion()))
                .body(view);
    }

    @Operation(summary = "Create a new book")
    @PutMapping("/{isbn}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookView> createBook(
            @PathVariable String isbn,
            @Valid @RequestBody CreateBookRequest request) {

        BookEntity book = bookCommandService.createBook(isbn, request);

        var newBookUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build().toUri();

        return ResponseEntity.created(newBookUri)
                .eTag(String.valueOf(book.getVersion()))
                .body(mapToBookView(book));
    }

    @Operation(summary = "Update an existing book")
    @PatchMapping("/{isbn}")
    public ResponseEntity<BookView> updateBook(
            @PathVariable String isbn,
            @RequestHeader("If-Match") Long expectedVersion,
            @Valid @RequestBody UpdateBookRequest request) {

        BookEntity book = bookCommandService.updateBook(isbn, request, expectedVersion);

        return ResponseEntity.ok()
                .eTag(String.valueOf(book.getVersion()))
                .body(mapToBookView(book));
    }

    @Operation(summary = "Delete a book")
    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBook(
            @PathVariable String isbn,
            @RequestHeader("If-Match") Long expectedVersion) {

        bookCommandService.deleteBook(isbn, expectedVersion);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove book photo")
    @DeleteMapping("/{isbn}/photo")
    public ResponseEntity<BookView> removeBookPhoto(
            @PathVariable String isbn,
            @RequestHeader("If-Match") Long expectedVersion) {

        BookEntity book = bookCommandService.removeBookPhoto(isbn, expectedVersion);

        return ResponseEntity.ok()
                .eTag(String.valueOf(book.getVersion()))
                .body(mapToBookView(book));
    }

    /**
     * ✅ CORRECTED: Map BookEntity to BookView
     * BookView.authors now contains author IDs (List<Long>) instead of names
     * This allows Feign Client to map correctly to BookDTO.authorIds
     */
    private BookView mapToBookView(BookEntity book) {
        BookView view = new BookView();
        view.setIsbn(book.getIsbnValue());
        view.setTitle(book.getTitleValue());
        view.setDescription(book.getDescriptionValue());
        view.setGenre(book.getGenreName());

        // ✅ CORRECTED: Return author IDs directly (not names)
        view.setAuthors(book.getAuthorIds() != null ? book.getAuthorIds() : List.of());

        view.setPhotoURI(book.getPhotoURI());
        view.setVersion(book.getVersion());
        return view;
    }
}