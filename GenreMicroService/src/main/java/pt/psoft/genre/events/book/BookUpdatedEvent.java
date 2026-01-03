package pt.psoft.genre.events.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

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

    @JsonProperty("authorNames")
    private List<String> authorNames;

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