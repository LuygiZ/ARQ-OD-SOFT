package pt.psoft.lending.repositories.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.lending.model.command.LendingEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Lending Entity
 */
@Repository
public interface LendingRepository extends JpaRepository<LendingEntity, Long> {

    /**
     * Find lending by lending number
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.lendingNumber.year = :year AND l.lendingNumber.sequence = :sequence")
    Optional<LendingEntity> findByLendingNumber(@Param("year") int year, @Param("sequence") int sequence);

    /**
     * Find all lendings by reader number
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.readerNumber = :readerNumber ORDER BY l.startDate DESC")
    List<LendingEntity> findByReaderNumber(@Param("readerNumber") String readerNumber);

    /**
     * Find all lendings by book ID (ISBN)
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.bookId = :bookId ORDER BY l.startDate DESC")
    List<LendingEntity> findByBookId(@Param("bookId") String bookId);

    /**
     * Find outstanding (not returned) lendings by reader number
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.readerNumber = :readerNumber AND l.returnedDate IS NULL")
    List<LendingEntity> findOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);

    /**
     * Find all overdue lendings
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.returnedDate IS NULL AND l.limitDate < :today")
    Page<LendingEntity> findOverdue(@Param("today") LocalDate today, Pageable pageable);

    /**
     * Count lendings from current year
     */
    @Query("SELECT COUNT(l) FROM LendingEntity l WHERE l.lendingNumber.year = :year")
    int countByYear(@Param("year") int year);

    /**
     * Calculate average lending duration for returned lendings
     */
    @Query(value = "SELECT AVG(l.returned_date - l.start_date) FROM lendings l WHERE l.returned_date IS NOT NULL", nativeQuery = true)
    Double getAverageDuration();

    /**
     * Find lendings by reader and book
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.readerNumber = :readerNumber AND l.bookId = :bookId")
    List<LendingEntity> findByReaderAndBook(
            @Param("readerNumber") String readerNumber,
            @Param("bookId") String bookId);

    /**
     * Find outstanding lendings by reader and book
     */
    @Query("SELECT l FROM LendingEntity l WHERE l.readerNumber = :readerNumber AND l.bookId = :bookId AND l.returnedDate IS NULL")
    List<LendingEntity> findOutstandingByReaderAndBook(
            @Param("readerNumber") String readerNumber,
            @Param("bookId") String bookId);

    /**
     * Search lendings with filters
     */
    @Query("SELECT l FROM LendingEntity l WHERE " +
           "(:readerNumber IS NULL OR l.readerNumber = :readerNumber) AND " +
           "(:bookId IS NULL OR l.bookId = :bookId) AND " +
           "(:startDate IS NULL OR l.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR l.startDate <= :endDate) " +
           "ORDER BY l.startDate DESC")
    Page<LendingEntity> searchLendings(
            @Param("readerNumber") String readerNumber,
            @Param("bookId") String bookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
