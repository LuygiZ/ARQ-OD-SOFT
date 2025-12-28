package pt.psoft.shared.events.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

/**
 * Domain Event emitted when an Author is deleted
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AuthorDeleted")
public class AuthorDeletedEvent extends DomainEvent {

    @JsonProperty("authorNumber")
    private Long authorNumber;

    @JsonProperty("version")
    private Long version;

    @Override
    public String getEventType() {
        return "DELETED";
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