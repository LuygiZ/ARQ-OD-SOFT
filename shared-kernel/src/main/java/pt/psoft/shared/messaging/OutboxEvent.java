package pt.psoft.shared.messaging;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String aggregateType;  // "Genre", "Author", "Book"

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;      // "GenreCreated", "GenreUpdated"

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
    }

    public OutboxEvent(String aggregateType, String aggregateId,
                       String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.status = OutboxStatus.PENDING;
    }
}