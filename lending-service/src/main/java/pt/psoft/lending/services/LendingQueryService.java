package pt.psoft.lending.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.psoft.lending.model.command.LendingEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Query Service Interface for Lending operations (Read Side - CQRS)
 */
public interface LendingQueryService {

    /**
     * Find lending by lending number
     */
    Optional<LendingEntity> findByLendingNumber(String lendingNumber);

    /**
     * Find all lendings by reader number
     */
    List<LendingEntity> findByReaderNumber(String readerNumber);

    /**
     * Find all lendings by book ID (ISBN)
     */
    List<LendingEntity> findByBookId(String bookId);

    /**
     * Find outstanding lendings by reader number
     */
    List<LendingEntity> findOutstandingByReaderNumber(String readerNumber);

    /**
     * Find all overdue lendings
     */
    Page<LendingEntity> findOverdue(Pageable pageable);

    /**
     * Get average lending duration
     */
    Double getAverageDuration();

    /**
     * Search lendings with filters
     */
    Page<LendingEntity> searchLendings(String readerNumber, String bookId,
                                       LocalDate startDate, LocalDate endDate,
                                       Pageable pageable);
}
