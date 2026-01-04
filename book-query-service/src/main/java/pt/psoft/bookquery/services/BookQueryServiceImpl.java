package pt.psoft.bookquery.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.bookquery.api.BookView;
import pt.psoft.bookquery.api.BookViewMapper;
import pt.psoft.bookquery.api.SearchBooksQuery;
import pt.psoft.bookquery.model.BookReadModel;
import pt.psoft.bookquery.repositories.BookQueryRepository;
import pt.psoft.shared.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookQueryServiceImpl implements BookQueryService {

    private final BookQueryRepository bookQueryRepository;
    private final BookViewMapper bookViewMapper;

    @Override
    @Cacheable(value = "books", key = "#isbn")
    public BookView findByIsbn(String isbn) {
        log.debug("Finding book by ISBN: {}", isbn);

        BookReadModel book = bookQueryRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException("Book with ISBN " + isbn + " not found"));

        return bookViewMapper.toBookView(book);
    }

    @Override
    @Cacheable(value = "books", key = "'all'")
    public List<BookView> findAll() {
        log.debug("Finding all books");
        return bookViewMapper.toBookView(bookQueryRepository.findAll());
    }

    @Override
    public List<BookView> findByTitle(String title) {
        log.debug("Finding books by title: {}", title);
        return bookViewMapper.toBookView(bookQueryRepository.findByTitleContaining(title));
    }

    @Override
    public List<BookView> findByGenre(String genreName) {
        log.debug("Finding books by genre: {}", genreName);
        return bookViewMapper.toBookView(bookQueryRepository.findByGenreName(genreName));
    }

    @Override
    public List<BookView> findByAuthorName(String authorName) {
        log.debug("Finding books by author name: {}", authorName);
        return bookViewMapper.toBookView(bookQueryRepository.findByAuthorName(authorName));
    }

    @Override
    public List<BookView> searchBooks(SearchBooksQuery query) {
        log.debug("Searching books with query: {}", query);

        return bookViewMapper.toBookView(bookQueryRepository.searchBooks(
                query.getTitle(),
                query.getGenre(),
                query.getAuthorName()
        ));
    }

    @Override
    public long countByGenre(String genreName) {
        log.debug("Counting books by genre: {}", genreName);
        return bookQueryRepository.countByGenreName(genreName);
    }
}
