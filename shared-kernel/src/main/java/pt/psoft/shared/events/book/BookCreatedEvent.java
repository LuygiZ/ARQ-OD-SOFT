package pt.psoft.shared.events.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

import java.util.List;

/**
 * Domain Event emitted when a Book is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BookCreated")
public class BookCreatedEvent extends DomainEvent {

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

    @JsonProperty("authorNames")
    private List<String> authorNames;

    @JsonProperty("photoURI")
    private String photoURI;

    @Override
    public String getEventType() {
        return "CREATED";
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
