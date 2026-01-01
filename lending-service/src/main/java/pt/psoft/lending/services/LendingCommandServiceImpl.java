package pt.psoft.lending.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.lending.api.dto.CreateLendingRequest;
import pt.psoft.lending.api.dto.ReturnLendingRequest;
import pt.psoft.lending.messaging.LendingEventPublisher;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.model.command.valueobjects.LendingNumber;
import pt.psoft.lending.repositories.jpa.LendingRepository;
import pt.psoft.shared.events.lending.LendingCreatedEvent;
import pt.psoft.shared.events.lending.LendingReturnedEvent;
import pt.psoft.shared.exceptions.BusinessException;
import pt.psoft.shared.exceptions.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of Lending Command Service
 * Handles all write operations for lendings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LendingCommandServiceImpl implements LendingCommandService {

    private final LendingRepository lendingRepository;
    private final LendingEventPublisher lendingEventPublisher;

    @Value("${lending.duration-in-days:14}")
    private int lendingDurationInDays;

    @Value("${lending.fine-value-per-day-in-cents:50}")
    private int fineValuePerDayInCents;

    @Value("${lending.max-outstanding-books:3}")
    private int maxOutstandingBooks;

    @Override
    @Transactional
    public LendingEntity createLending(CreateLendingRequest request) {
        log.info("Creating lending for book {} and reader {}", request.getIsbn(), request.getReaderNumber());

        // Validate outstanding lendings
        List<LendingEntity> outstandingLendings = lendingRepository.findOutstandingByReaderNumber(request.getReaderNumber());

        // Business rule: cannot create lending if reader has overdue books
        for (LendingEntity lending : outstandingLendings) {
            if (lending.isOverdue()) {
                throw new BusinessException("Reader has book(s) past their due date. Cannot create new lending.");
            }
        }

        // Business rule: cannot create lending if reader has max outstanding books
        if (outstandingLendings.size() >= maxOutstandingBooks) {
            throw new BusinessException("Reader already has " + maxOutstandingBooks + " outstanding books. Cannot create new lending.");
        }

        // Get next sequence number
        int currentYear = LocalDate.now().getYear();
        int nextSequence = lendingRepository.countByYear(currentYear) + 1;

        // TODO: In a real scenario, we would validate the book and reader exist
        // by calling Book Service and Reader Service (with Circuit Breaker)
        // For now, we assume the caller has validated these

        LendingEntity lending = new LendingEntity(
                request.getIsbn(),
                1L, // TODO: Get reader ID from Reader Service
                request.getReaderNumber(),
                nextSequence,
                lendingDurationInDays,
                fineValuePerDayInCents
        );

        LendingEntity savedLending = lendingRepository.save(lending);
        log.info("Lending created with number: {}", savedLending.getLendingNumberValue());

        // Publish domain event
        LendingCreatedEvent event = new LendingCreatedEvent(
                savedLending.getLendingNumberValue(),
                savedLending.getBookId(),
                savedLending.getReaderId(),
                savedLending.getReaderNumber(),
                savedLending.getStartDate(),
                savedLending.getLimitDate()
        );
        lendingEventPublisher.publishLendingCreated(event);

        return savedLending;
    }

    @Override
    @Transactional
    public LendingEntity returnLending(String lendingNumber, ReturnLendingRequest request, Long expectedVersion) {
        log.info("Returning lending {} with rating {} and comment length {}",
                lendingNumber, request.getRating(),
                request.getComment() != null ? request.getComment().length() : 0);

        // Parse lending number
        LendingNumber ln = LendingNumber.parse(lendingNumber);

        // Find lending
        LendingEntity lending = lendingRepository.findByLendingNumber(ln.getYear(), ln.getSequence())
                .orElseThrow(() -> new NotFoundException("Lending with number " + lendingNumber + " not found"));

        // Validate lending is active
        if (!lending.isActive()) {
            throw new BusinessException("Lending " + lendingNumber + " has already been returned");
        }

        // Mark as returned with comment and rating
        lending.setReturned(expectedVersion, request.getComment(), request.getRating());

        LendingEntity savedLending = lendingRepository.save(lending);
        log.info("Lending {} returned successfully", lendingNumber);

        // Publish domain event - this triggers Review Service and Book Service
        LendingReturnedEvent event = new LendingReturnedEvent(
                savedLending.getLendingNumberValue(),
                savedLending.getBookId(),
                savedLending.getReaderId(),
                savedLending.getReaderNumber(),
                savedLending.getReturnedDate(),
                savedLending.getComment(),
                savedLending.getRating(),
                savedLending.getDaysOverdue(),
                savedLending.getFineAmountInCents()
        );
        lendingEventPublisher.publishLendingReturned(event);

        return savedLending;
    }
}
