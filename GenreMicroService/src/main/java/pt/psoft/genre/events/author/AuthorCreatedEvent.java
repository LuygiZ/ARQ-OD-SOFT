package pt.psoft.genre.events.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

/**
 * Domain Event emitted when an Author is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AuthorCreated")
public class AuthorCreatedEvent extends DomainEvent {

    @JsonProperty("authorNumber")
    private Long authorNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("photoURI")
    private String photoURI;

    @Override
    public String getEventType() {
        return "CREATED";
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