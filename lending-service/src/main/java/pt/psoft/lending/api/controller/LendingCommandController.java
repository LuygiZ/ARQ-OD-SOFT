package pt.psoft.lending.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.lending.api.dto.*;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.services.LendingCommandService;

/**
 * REST Controller for Lending Command operations (Write Side - CQRS)
 * Implements Student C functionality: return book with comment and rating
 */
@Tag(name = "Lendings - Commands", description = "Endpoints for creating and returning lendings")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lendings")
public class LendingCommandController {

    private final LendingCommandService lendingCommandService;

    @Operation(summary = "Create a new lending")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LendingView> createLending(@Valid @RequestBody CreateLendingRequest request) {
        LendingEntity lending = lendingCommandService.createLending(request);

        var newLendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{lendingNumber}")
                .buildAndExpand(lending.getLendingNumberValue())
                .toUri();

        return ResponseEntity.created(newLendingUri)
                .eTag(String.valueOf(lending.getVersion()))
                .body(mapToLendingView(lending));
    }

    /**
     * Return a lending with comment and rating
     * This is the key endpoint for Student C functionality
     *
     * POST /api/v1/lendings/{year}/{sequence}/return
     * Body: { "comment": "Great book!", "rating": 8 }
     * Header: If-Match: {version}
     */
    @Operation(summary = "Return a lending with comment and rating",
               description = "Returns a book and allows the reader to leave a comment and rating (0-10)")
    @PostMapping("/{year}/{sequence}/return")
    public ResponseEntity<LendingReturnView> returnLending(
            @PathVariable @Parameter(description = "Lending year (e.g., 2025)") int year,
            @PathVariable @Parameter(description = "Lending sequence (e.g., 1)") int sequence,
            @RequestHeader("If-Match") @Parameter(description = "Expected version for optimistic locking") Long expectedVersion,
            @Valid @RequestBody ReturnLendingRequest request) {

        String lendingNumber = year + "/" + sequence;
        LendingEntity lending = lendingCommandService.returnLending(lendingNumber, request, expectedVersion);

        LendingReturnView response = LendingReturnView.builder()
                .lendingNumber(lending.getLendingNumberValue())
                .returnDate(lending.getReturnedDate())
                .daysOverdue(lending.getDaysOverdue())
                .fineAmountInCents(lending.getFineAmountInCents())
                .review(LendingReturnView.ReviewInfo.builder()
                        .comment(lending.getComment())
                        .rating(lending.getRating())
                        .build())
                .build();

        return ResponseEntity.ok()
                .eTag(String.valueOf(lending.getVersion()))
                .body(response);
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
