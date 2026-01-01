package pt.psoft.book.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.book.model.query.BookReview;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BookReview (Query Side)
 */
@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    /**
     * Find all reviews for a book by ISBN
     */
    List<BookReview> findByIsbnOrderByReturnDateDesc(String isbn);

    /**
     * Find all reviews for a book with pagination
     */
    Page<BookReview> findByIsbnOrderByReturnDateDesc(String isbn, Pageable pageable);

    /**
     * Find reviews by reader number
     */
    List<BookReview> findByReaderNumber(String readerNumber);

    /**
     * Check if a review already exists for a lending
     */
    boolean existsByLendingNumber(String lendingNumber);

    /**
     * Find review by lending number
     */
    Optional<BookReview> findByLendingNumber(String lendingNumber);

    /**
     * Count reviews for a book
     */
    long countByIsbn(String isbn);

    /**
     * Calculate average rating for a book
     */
    @Query("SELECT AVG(r.rating) FROM BookReview r WHERE r.isbn = :isbn")
    Double getAverageRatingByIsbn(@Param("isbn") String isbn);

    /**
     * Get top-rated books
     */
    @Query("SELECT r.isbn, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
           "FROM BookReview r GROUP BY r.isbn ORDER BY avgRating DESC")
    List<Object[]> findTopRatedBooks(Pageable pageable);
}
