package pt.psoft.bookcommand.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.bookcommand.model.BookEntity;

import java.util.Optional;

/**
 * Repository for Book Command Model (Write Side)
 */
@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    @Query("SELECT b FROM BookEntity b WHERE b.isbn.isbn = :isbn")
    Optional<BookEntity> findByIsbn(@Param("isbn") String isbn);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BookEntity b WHERE b.isbn.isbn = :isbn")
    boolean existsByIsbn(@Param("isbn") String isbn);

    @Query("DELETE FROM BookEntity b WHERE b.isbn.isbn = :isbn")
    void deleteByIsbn(@Param("isbn") String isbn);
}
