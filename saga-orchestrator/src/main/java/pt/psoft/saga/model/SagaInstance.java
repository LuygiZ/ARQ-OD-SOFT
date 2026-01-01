package pt.psoft.saga.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Saga Instance - Represents a distributed transaction
 * Stored in Redis with TTL of 1 hour
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("saga")
public class SagaInstance implements Serializable {

    @Id
    private String sagaId;

    private SagaState state;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    // Original request data
    private String requestPayload;

    // Created entity IDs (for compensation)
    private Long genreId;
    private Long authorNumber;
    private Long bookId;

    // Response data
    private String genreResponse;
    private String authorResponse;
    private String bookResponse;

    // Error tracking
    private String errorMessage;
    private Integer retryCount;

    // Step history
    @Builder.Default
    private List<SagaStep> steps = new ArrayList<>();

    // TTL: 1 hour (in seconds)
    @TimeToLive
    private Long ttl = 3600L;

    /**
     * Create new Saga instance
     */
    public static SagaInstance create(String requestPayload) {
        return SagaInstance.builder()
                .sagaId(UUID.randomUUID().toString())
                .state(SagaState.STARTED)
                .startedAt(LocalDateTime.now())
                .requestPayload(requestPayload)
                .retryCount(0)
                .steps(new ArrayList<>())
                .build();
    }

    /**
     * Add step to history
     */
    public void addStep(SagaStep step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }

    /**
     * Mark as completed
     */
    public void complete() {
        this.state = SagaState.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void fail(String errorMessage) {
        this.state = SagaState.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Start compensation
     */
    public void startCompensation() {
        this.state = SagaState.COMPENSATING;
    }

    /**
     * Mark as compensated
     */
    public void compensated() {
        this.state = SagaState.COMPENSATED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count
     */
    public void incrementRetry() {
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        this.retryCount++;
    }
}