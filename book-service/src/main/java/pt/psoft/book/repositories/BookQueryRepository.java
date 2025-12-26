package pt.psoft.book.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.book.model.query.BookReadModel;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Book Query Model (Read Side - CQRS)
 * All queries use denormalized data - NO JOINS!
 */
@Repository
public interface BookQueryRepository extends JpaRepository<BookReadModel, String> {

    @Cacheable(value = "books", key = "#isbn", unless = "#result == null")
    Optional<BookReadModel> findByIsbn(String isbn);

    @Cacheable(value = "books", key = "'all'",unless = "#result == null")
    @Override
    List<BookReadModel> findAll();

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("SELECT b FROM BookReadModel b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<BookReadModel> findByTitleContaining(@Param("title") String title);

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("SELECT b FROM BookReadModel b WHERE LOWER(b.genreName) = LOWER(:genreName)")
    List<BookReadModel> findByGenreName(@Param("genreName") String genreName);

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("SELECT b FROM BookReadModel b WHERE LOWER(b.authorNames) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<BookReadModel> findByAuthorName(@Param("authorName") String authorName);

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("SELECT b FROM BookReadModel b WHERE b.authorIds LIKE CONCAT('%', :authorId, '%')")
    List<BookReadModel> findByAuthorId(@Param("authorId") Long authorId);

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("""
        SELECT b FROM BookReadModel b 
        WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:genre IS NULL OR LOWER(b.genreName) = LOWER(:genre))
        AND (:authorName IS NULL OR LOWER(b.authorNames) LIKE LOWER(CONCAT('%', :authorName, '%')))
        ORDER BY b.title
    """)
    List<BookReadModel> searchBooks(
            @Param("title") String title,
            @Param("genre") String genre,
            @Param("authorName") String authorName
    );

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("""
        SELECT b FROM BookReadModel b 
        WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:genre IS NULL OR LOWER(b.genreName) = LOWER(:genre))
        AND (:authorName IS NULL OR LOWER(b.authorNames) LIKE LOWER(CONCAT('%', :authorName, '%')))
    """)
    Page<BookReadModel> searchBooksPageable(
            @Param("title") String title,
            @Param("genre") String genre,
            @Param("authorName") String authorName,
            Pageable pageable
    );

    @Cacheable(value = "books", unless = "#result == null || #result.isEmpty()")
    @Query("SELECT COUNT(b) FROM BookReadModel b WHERE LOWER(b.genreName) = LOWER(:genreName)")
    long countByGenreName(@Param("genreName") String genreName);
}