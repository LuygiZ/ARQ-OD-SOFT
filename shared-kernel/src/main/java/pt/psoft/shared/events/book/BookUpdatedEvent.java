package pt.psoft.shared.events.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

import java.util.List;

/**
 * Domain Event emitted when a Book is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BookUpdated")
public class BookUpdatedEvent extends DomainEvent {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("genre")
    private String genre;

    @JsonProperty("authorIds")
    private List<Long> authorIds;

    @JsonProperty("photoURI")
    private String photoURI;

    @JsonProperty("version")
    private Long version;

    @Override
    public String getEventType() {
        return "UPDATED";
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