package pt.psoft.genre.events.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

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