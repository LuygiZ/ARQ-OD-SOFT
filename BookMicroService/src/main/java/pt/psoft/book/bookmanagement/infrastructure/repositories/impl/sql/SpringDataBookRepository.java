package pt.psoft.book.bookmanagement.infrastructure.repositories.impl.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.IsbnSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataBookRepository extends CrudRepository<BookSqlEntity, IsbnSqlEntity> {

    @Query("SELECT b " +
            "FROM BookSqlEntity b " +
            "WHERE b.isbn.isbn = :isbn")
    Optional<BookSqlEntity> findByIsbn(@Param("isbn") String isbn);

    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO(b, COUNT(l)) " +
            "FROM BookSqlEntity b " +
            "JOIN LendingSqlEntity l ON l.book = b " +
            "WHERE l.startDate > :oneYearAgo " +
            "GROUP BY b " +
            "ORDER BY COUNT(l) DESC")
    Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable);

    @Query("SELECT b " +
            "FROM BookSqlEntity b " +
            "WHERE b.genre.genre LIKE %:genre%")
    List<BookSqlEntity> findByGenre(@Param("genre") String genre);

    @Query("SELECT b FROM BookSqlEntity b WHERE b.title.title LIKE %:title%")
    List<BookSqlEntity> findByTitle(@Param("title") String title);

    @Query(value =
            "SELECT b.* " +
                    "FROM Book b " +
                    "JOIN BOOK_AUTHORS on b.pk = BOOK_AUTHORS.BOOK_PK " +
                    "JOIN AUTHOR a on BOOK_AUTHORS.AUTHORS_AUTHOR_NUMBER = a.AUTHOR_NUMBER " +
                    "WHERE a.NAME LIKE :authorName"
            , nativeQuery = true)
    List<BookSqlEntity> findByAuthorName(@Param("authorName") String authorName);

    @Query(value =
            "SELECT b.* " +
                    "FROM Book b " +
                    "JOIN BOOK_AUTHORS on b.pk = BOOK_AUTHORS.BOOK_PK " +
                    "JOIN AUTHOR a on BOOK_AUTHORS.AUTHORS_AUTHOR_NUMBER = a.AUTHOR_NUMBER " +
                    "WHERE a.AUTHOR_NUMBER = :authorNumber "
            , nativeQuery = true)
    List<BookSqlEntity> findBooksByAuthorNumber(Long authorNumber);

}
