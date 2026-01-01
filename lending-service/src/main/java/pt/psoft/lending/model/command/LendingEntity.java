package pt.psoft.lending.model.command;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.hibernate.StaleObjectStateException;
import pt.psoft.lending.model.command.valueobjects.LendingNumber;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Lending Entity (Command Model - Write Side)
 * Represents the source of truth for Lending aggregate
 */
@Entity
@Table(name = "lendings")
@Getter
public class LendingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Embedded
    private LendingNumber lendingNumber;

    @Column(name = "book_id", nullable = false)
    private String bookId;  // ISBN

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "reader_number", nullable = false)
    private String readerNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "limit_date", nullable = false)
    private LocalDate limitDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "comment", length = 1024)
    private String comment;

    @Column(name = "rating")
    @Min(0)
    @Max(10)
    private Integer rating;

    @Column(name = "fine_value_per_day_cents")
    private Integer fineValuePerDayInCents;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    protected LendingEntity() {
        // For JPA
    }

    public LendingEntity(String bookId, Long readerId, String readerNumber,
                         int sequenceNumber, int lendingDurationInDays, int fineValuePerDayInCents) {
        this.bookId = Objects.requireNonNull(bookId, "Book ID cannot be null");
        this.readerId = Objects.requireNonNull(readerId, "Reader ID cannot be null");
        this.readerNumber = Objects.requireNonNull(readerNumber, "Reader Number cannot be null");

        this.lendingNumber = new LendingNumber(sequenceNumber);
        this.startDate = LocalDate.now();
        this.limitDate = LocalDate.now().plusDays(lendingDurationInDays);
        this.returnedDate = null;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    /**
     * Mark the lending as returned with optional comment and rating
     * This is the key method for Student C functionality
     */
    public void setReturned(Long expectedVersion, String comment, Integer rating) {
        if (this.returnedDate != null) {
            throw new IllegalStateException("Book has already been returned!");
        }

        // Version check for optimistic locking
        if (expectedVersion != null && this.version != null && !this.version.equals(expectedVersion)) {
            throw new StaleObjectStateException("Lending", this.pk);
        }

        // Validate rating if provided
        if (rating != null) {
            if (rating < 0 || rating > 10) {
                throw new IllegalArgumentException("Rating must be between 0 and 10");
            }
            this.rating = rating;
        }

        // Comment can be empty but not null according to requirements
        if (comment != null) {
            this.comment = comment;
        }

        this.returnedDate = LocalDate.now();
    }

    /**
     * Get the lending number as string
     */
    public String getLendingNumberValue() {
        return lendingNumber != null ? lendingNumber.toString() : null;
    }

    /**
     * Calculate days delayed (overdue)
     */
    public int getDaysDelayed() {
        LocalDate referenceDate = returnedDate != null ? returnedDate : LocalDate.now();
        int days = (int) ChronoUnit.DAYS.between(limitDate, referenceDate);
        return Math.max(days, 0);
    }

    /**
     * Calculate days until return (if not overdue)
     */
    public Integer getDaysUntilReturn() {
        if (returnedDate != null) {
            return null;
        }
        int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), limitDate);
        return days >= 0 ? days : null;
    }

    /**
     * Calculate days overdue
     */
    public Integer getDaysOverdue() {
        int days = getDaysDelayed();
        return days > 0 ? days : null;
    }

    /**
     * Calculate fine amount in cents
     */
    public Integer getFineAmountInCents() {
        int daysDelayed = getDaysDelayed();
        return daysDelayed > 0 ? fineValuePerDayInCents * daysDelayed : null;
    }

    /**
     * Check if lending is overdue
     */
    public boolean isOverdue() {
        return getDaysDelayed() > 0;
    }

    /**
     * Check if lending is active (not returned)
     */
    public boolean isActive() {
        return returnedDate == null;
    }
}
