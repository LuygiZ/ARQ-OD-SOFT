package pt.psoft.genre.events.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

/**
 * Domain Event emitted when an Author is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AuthorUpdated")
public class AuthorUpdatedEvent extends DomainEvent {

    @JsonProperty("authorNumber")
    private Long authorNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bio")
    private String bio;

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
        return "AUTHOR";
    }

    @Override
    public String getAggregateId() {
        return authorNumber != null ? authorNumber.toString() : null;
    }
}