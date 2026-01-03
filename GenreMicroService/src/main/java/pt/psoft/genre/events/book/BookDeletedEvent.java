package pt.psoft.genre.events.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

/**
 * Domain Event emitted when a Book is deleted
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BookDeleted")
public class BookDeletedEvent extends DomainEvent {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("version")
    private Long version;

    @Override
    public String getEventType() {
        return "DELETED";
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
