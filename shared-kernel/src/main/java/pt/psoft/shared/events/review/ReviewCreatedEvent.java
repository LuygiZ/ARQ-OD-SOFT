package pt.psoft.shared.events.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain Event emitted when a Review is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ReviewCreated")
public class ReviewCreatedEvent extends DomainEvent {

    @JsonProperty("reviewId")
    private Long reviewId;

    @JsonProperty("bookId")
    private String bookId;

    @JsonProperty("readerId")
    private Long readerId;

    @JsonProperty("lendingNumber")
    private String lendingNumber;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("rating")
    private Integer rating;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Override
    public String getEventType() {
        return "CREATED";
    }

    @Override
    public String getAggregateType() {
        return "REVIEW";
    }

    @Override
    public String getAggregateId() {
        return reviewId != null ? reviewId.toString() : null;
    }
}
