package pt.psoft.shared.events.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

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