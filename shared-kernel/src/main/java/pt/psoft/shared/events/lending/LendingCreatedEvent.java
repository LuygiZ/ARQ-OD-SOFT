package pt.psoft.shared.events.lending;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

import java.time.LocalDate;

/**
 * Domain Event emitted when a Lending is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LendingCreated")
public class LendingCreatedEvent extends DomainEvent {

    @JsonProperty("lendingNumber")
    private String lendingNumber;

    @JsonProperty("bookId")
    private String bookId;

    @JsonProperty("readerId")
    private Long readerId;

    @JsonProperty("readerNumber")
    private String readerNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("startDate")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("limitDate")
    private LocalDate limitDate;

    @Override
    public String getEventType() {
        return "CREATED";
    }

    @Override
    public String getAggregateType() {
        return "LENDING";
    }

    @Override
    public String getAggregateId() {
        return lendingNumber;
    }
}
