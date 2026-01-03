package pt.psoft.book.shared.messaging;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.psoft.shared.messaging.OutboxStatus;

import java.time.LocalDateTime;

/**
 * Outbox Pattern - ensures atomicity between DB update and event publishing
 * Each microservice should have this table in its database
 */
@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String aggregateType;  // "GENRE", "AUTHOR", "BOOK"

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;      // "CREATED", "UPDATED", "DELETED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;        // JSON do event

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private Integer retryCount = 0;

    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OutboxStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    public OutboxEvent(String aggregateType, String aggregateId,
                       String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    // State management methods

    /**
     * Mark event as successfully published
     */
    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Mark event as failed after max retries
     */
    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }

    /**
     * Increment retry counter
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * Set error message from exception
     */
    public void setError(Exception e) {
        this.errorMessage = e.getMessage();
    }

    /**
     * Check if event can be retried
     */
    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries;
    }

    /**
     * Reset for manual retry
     */
    public void resetForRetry() {
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.errorMessage = null;
        this.publishedAt = null;
    }
}