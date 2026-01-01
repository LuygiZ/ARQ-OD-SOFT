package pt.psoft.shared.events.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

/**
 * Domain Event emitted when a Book's rating is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BookRatingUpdated")
public class BookRatingUpdatedEvent extends DomainEvent {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("averageRating")
    private Double averageRating;

    @JsonProperty("totalReviews")
    private Integer totalReviews;

    @Override
    public String getEventType() {
        return "RATING_UPDATED";
    }

    @Override
    public String getAggregateType() {
        return "BOOK";
    }

    @Override
    public String getAggregateId() {
        return isbn;
    }
}
