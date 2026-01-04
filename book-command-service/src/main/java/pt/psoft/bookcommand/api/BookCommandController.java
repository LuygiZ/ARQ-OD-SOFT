package pt.psoft.bookcommand.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.bookcommand.model.BookEntity;
import pt.psoft.bookcommand.services.BookCommandService;
import pt.psoft.shared.dto.book.CreateBookRequest;
import pt.psoft.shared.utils.IsbnGenerator;

import java.util.Collections;

@Tag(name = "Books - Commands", description = "Endpoints for book write operations (CQRS Command Side)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookCommandController {

    private final BookCommandService bookCommandService;

    @Operation(summary = "Create a new book (auto-generate ISBN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookView> createBookAutoISBN(
            @Valid @RequestBody CreateBookRequest request) {

        String isbn = IsbnGenerator.generateValidIsbn();

        BookEntity book = bookCommandService.createBook(isbn, request);

        var newBookUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{isbn}")
                .buildAndExpand(book.getIsbnValue())
                .toUri();

        return ResponseEntity.created(newBookUri)
                .eTag(String.valueOf(book.getVersion()))
                .body(mapToBookView(book));
    }

    @Operation(summary = "Create a new book with specific ISBN")
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
            @RequestHeader(value = "If-Match", required = false, defaultValue = "0") Long expectedVersion,
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
            @RequestHeader(value = "If-Match", required = false, defaultValue = "0") Long expectedVersion) {

        bookCommandService.deleteBook(isbn, expectedVersion);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove book photo")
    @DeleteMapping("/{isbn}/photo")
    public ResponseEntity<BookView> removeBookPhoto(
            @PathVariable String isbn,
            @RequestHeader(value = "If-Match", required = false, defaultValue = "0") Long expectedVersion) {

        BookEntity book = bookCommandService.removeBookPhoto(isbn, expectedVersion);

        return ResponseEntity.ok()
                .eTag(String.valueOf(book.getVersion()))
                .body(mapToBookView(book));
    }

    private BookView mapToBookView(BookEntity book) {
        BookView view = new BookView();
        view.setIsbn(book.getIsbnValue());
        view.setTitle(book.getTitleValue());
        view.setDescription(book.getDescriptionValue());
        view.setGenre(book.getGenreName());
        view.setAuthors(Collections.emptyList());
        view.setPhotoURI(book.getPhotoURI());
        view.setVersion(book.getVersion());
        return view;
    }
}
