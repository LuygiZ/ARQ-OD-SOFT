package pt.psoft.lending.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.lending.api.dto.LendingView;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.services.LendingQueryService;
import pt.psoft.shared.exceptions.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Lending Query operations (Read Side - CQRS)
 */
@Tag(name = "Lendings - Queries", description = "Endpoints for querying lendings")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lendings")
public class LendingQueryController {

    private final LendingQueryService lendingQueryService;

    @Operation(summary = "Get lending by lending number")
    @GetMapping("/{year}/{sequence}")
    public ResponseEntity<LendingView> getLending(
            @PathVariable @Parameter(description = "Lending year (e.g., 2025)") int year,
            @PathVariable @Parameter(description = "Lending sequence (e.g., 1)") int sequence) {

        String lendingNumber = year + "/" + sequence;
        LendingEntity lending = lendingQueryService.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new NotFoundException("Lending with number " + lendingNumber + " not found"));

        return ResponseEntity.ok()
                .eTag(String.valueOf(lending.getVersion()))
                .body(mapToLendingView(lending));
    }

    @Operation(summary = "Get lendings by reader number")
    @GetMapping("/reader/{readerNumber}")
    public ResponseEntity<List<LendingView>> getLendingsByReader(
            @PathVariable @Parameter(description = "Reader number") String readerNumber) {

        List<LendingEntity> lendings = lendingQueryService.findByReaderNumber(readerNumber);
        List<LendingView> views = lendings.stream().map(this::mapToLendingView).toList();

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Get lendings by book ISBN")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<LendingView>> getLendingsByBook(
            @PathVariable @Parameter(description = "Book ISBN") String bookId) {

        List<LendingEntity> lendings = lendingQueryService.findByBookId(bookId);
        List<LendingView> views = lendings.stream().map(this::mapToLendingView).toList();

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Get outstanding lendings by reader number")
    @GetMapping("/reader/{readerNumber}/outstanding")
    public ResponseEntity<List<LendingView>> getOutstandingLendings(
            @PathVariable @Parameter(description = "Reader number") String readerNumber) {

        List<LendingEntity> lendings = lendingQueryService.findOutstandingByReaderNumber(readerNumber);
        List<LendingView> views = lendings.stream().map(this::mapToLendingView).toList();

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Get overdue lendings")
    @GetMapping("/overdue")
    public ResponseEntity<Page<LendingView>> getOverdueLendings(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<LendingEntity> lendings = lendingQueryService.findOverdue(pageable);
        Page<LendingView> views = lendings.map(this::mapToLendingView);

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Get average lending duration")
    @GetMapping("/stats/average-duration")
    public ResponseEntity<Double> getAverageDuration() {
        Double avg = lendingQueryService.getAverageDuration();
        return ResponseEntity.ok(avg);
    }

    @Operation(summary = "Search lendings with filters")
    @GetMapping("/search")
    public ResponseEntity<Page<LendingView>> searchLendings(
            @RequestParam(required = false) @Parameter(description = "Reader number") String readerNumber,
            @RequestParam(required = false) @Parameter(description = "Book ISBN") String bookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<LendingEntity> lendings = lendingQueryService.searchLendings(
                readerNumber, bookId, startDate, endDate, pageable);
        Page<LendingView> views = lendings.map(this::mapToLendingView);

        return ResponseEntity.ok(views);
    }

    private LendingView mapToLendingView(LendingEntity lending) {
        return LendingView.builder()
                .lendingNumber(lending.getLendingNumberValue())
                .bookId(lending.getBookId())
                .readerNumber(lending.getReaderNumber())
                .startDate(lending.getStartDate())
                .limitDate(lending.getLimitDate())
                .returnedDate(lending.getReturnedDate())
                .daysUntilReturn(lending.getDaysUntilReturn())
                .daysOverdue(lending.getDaysOverdue())
                .fineAmountInCents(lending.getFineAmountInCents())
                .comment(lending.getComment())
                .rating(lending.getRating())
                .version(lending.getVersion())
                .build();
    }
}
