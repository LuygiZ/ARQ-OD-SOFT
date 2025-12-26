package pt.psoft.book.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.book.api.BookView;
import pt.psoft.book.api.CreateBookRequest;
import pt.psoft.book.api.UpdateBookRequest;
import pt.psoft.book.model.command.BookEntity;
import pt.psoft.book.services.BookCommandService;

import java.util.Collections;

@Tag(name = "Books - Commands", description = "Endpoints for book write operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookCommandController {

    private final BookCommandService bookCommandService;

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