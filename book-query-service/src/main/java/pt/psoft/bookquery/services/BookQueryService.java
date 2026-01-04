package pt.psoft.bookquery.services;

import pt.psoft.bookquery.api.BookView;
import pt.psoft.bookquery.api.SearchBooksQuery;

import java.util.List;

public interface BookQueryService {

    BookView findByIsbn(String isbn);

    List<BookView> findAll();

    List<BookView> findByTitle(String title);

    List<BookView> findByGenre(String genreName);

    List<BookView> findByAuthorName(String authorName);

    List<BookView> searchBooks(SearchBooksQuery query);

    long countByGenre(String genreName);
}
