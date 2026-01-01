package pt.psoft.lending.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.model.command.valueobjects.LendingNumber;
import pt.psoft.lending.repositories.jpa.LendingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementation of Lending Query Service
 * Handles all read operations for lendings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LendingQueryServiceImpl implements LendingQueryService {

    private final LendingRepository lendingRepository;

    @Override
    @Cacheable(value = "lendings", key = "#lendingNumber")
    public Optional<LendingEntity> findByLendingNumber(String lendingNumber) {
        log.debug("Finding lending by number: {}", lendingNumber);
        LendingNumber ln = LendingNumber.parse(lendingNumber);
        return lendingRepository.findByLendingNumber(ln.getYear(), ln.getSequence());
    }

    @Override
    public List<LendingEntity> findByReaderNumber(String readerNumber) {
        log.debug("Finding lendings by reader number: {}", readerNumber);
        return lendingRepository.findByReaderNumber(readerNumber);
    }

    @Override
    public List<LendingEntity> findByBookId(String bookId) {
        log.debug("Finding lendings by book ID: {}", bookId);
        return lendingRepository.findByBookId(bookId);
    }

    @Override
    public List<LendingEntity> findOutstandingByReaderNumber(String readerNumber) {
        log.debug("Finding outstanding lendings by reader number: {}", readerNumber);
        return lendingRepository.findOutstandingByReaderNumber(readerNumber);
    }

    @Override
    public Page<LendingEntity> findOverdue(Pageable pageable) {
        log.debug("Finding overdue lendings");
        return lendingRepository.findOverdue(LocalDate.now(), pageable);
    }

    @Override
    @Cacheable(value = "lending-stats", key = "'averageDuration'")
    public Double getAverageDuration() {
        log.debug("Calculating average lending duration");
        Double avg = lendingRepository.getAverageDuration();
        if (avg == null) {
            return 0.0;
        }
        return Double.valueOf(String.format(Locale.US, "%.1f", avg));
    }

    @Override
    public Page<LendingEntity> searchLendings(String readerNumber, String bookId,
                                               LocalDate startDate, LocalDate endDate,
                                               Pageable pageable) {
        log.debug("Searching lendings with filters - reader: {}, book: {}, start: {}, end: {}",
                readerNumber, bookId, startDate, endDate);
        return lendingRepository.searchLendings(readerNumber, bookId, startDate, endDate, pageable);
    }
}
