package pt.psoft.book.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import pt.psoft.book.api.BookView;
import pt.psoft.book.api.ReviewView;
import pt.psoft.book.api.SearchBooksQuery;
import pt.psoft.book.model.query.BookReview;
import pt.psoft.book.repositories.BookReviewRepository;
import pt.psoft.book.services.BookQueryService;

import java.util.List;

/**
 * REST Controller for Book Queries (Read Operations)
 * GET endpoints using denormalized read model
 */
@Tag(name = "Books - Queries", description = "Endpoints for book read operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookQueryController {

    private final BookQueryService bookQueryService;
    private final BookReviewRepository bookReviewRepository;

    @Operation(summary = "Get book by ISBN")
    @GetMapping("/{isbn}")
    public ResponseEntity<BookView> findByIsbn(@PathVariable String isbn) {
        BookView book = bookQueryService.findByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Get all books")
    @GetMapping
    public ResponseEntity<List<BookView>> findAll() {
        List<BookView> books = bookQueryService.findAll();
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Search books by title")
    @GetMapping("/search/title")
    public ResponseEntity<List<BookView>> findByTitle(@RequestParam String title) {
        List<BookView> books = bookQueryService.findByTitle(title);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Search books by genre")
    @GetMapping("/search/genre")
    public ResponseEntity<List<BookView>> findByGenre(@RequestParam String genre) {
        List<BookView> books = bookQueryService.findByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Search books by author name")
    @GetMapping("/search/author")
    public ResponseEntity<List<BookView>> findByAuthorName(@RequestParam String authorName) {
        List<BookView> books = bookQueryService.findByAuthorName(authorName);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Search books with multiple criteria")
    @PostMapping("/search")
    public ResponseEntity<List<BookView>> searchBooks(@RequestBody SearchBooksQuery query) {
        List<BookView> books = bookQueryService.searchBooks(query);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Count books by genre")
    @GetMapping("/count/genre")
    public ResponseEntity<Long> countByGenre(@RequestParam String genre) {
        long count = bookQueryService.countByGenre(genre);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get reviews for a book by ISBN")
    @GetMapping("/{isbn}/reviews")
    public ResponseEntity<Page<ReviewView>> getBookReviews(
            @PathVariable String isbn,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BookReview> reviews = bookReviewRepository.findByIsbnOrderByReturnDateDesc(isbn, pageable);
        Page<ReviewView> reviewViews = reviews.map(this::mapToReviewView);
        return ResponseEntity.ok(reviewViews);
    }

    @Operation(summary = "Get all reviews for a book (no pagination)")
    @GetMapping("/{isbn}/reviews/all")
    public ResponseEntity<List<ReviewView>> getAllBookReviews(@PathVariable String isbn) {
        List<BookReview> reviews = bookReviewRepository.findByIsbnOrderByReturnDateDesc(isbn);
        List<ReviewView> reviewViews = reviews.stream()
                .map(this::mapToReviewView)
                .toList();
        return ResponseEntity.ok(reviewViews);
    }

    private ReviewView mapToReviewView(BookReview review) {
        return ReviewView.builder()
                .id(review.getId())
                .lendingNumber(review.getLendingNumber())
                .isbn(review.getIsbn())
                .readerNumber(review.getReaderNumber())
                .comment(review.getComment())
                .rating(review.getRating())
                .returnDate(review.getReturnDate())
                .build();
    }
}