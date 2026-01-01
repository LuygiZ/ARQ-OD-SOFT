package pt.psoft.shared.events.lending;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import pt.psoft.shared.events.DomainEvent;

import java.time.LocalDate;

/**
 * Domain Event emitted when a Lending is returned with review
 * This is the key event for Student C functionality
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LendingReturned")
public class LendingReturnedEvent extends DomainEvent {

    @JsonProperty("lendingNumber")
    private String lendingNumber;

    @JsonProperty("bookId")
    private String bookId;

    @JsonProperty("readerId")
    private Long readerId;

    @JsonProperty("readerNumber")
    private String readerNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("returnDate")
    private LocalDate returnDate;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("rating")
    private Integer rating;

    @JsonProperty("daysOverdue")
    private Integer daysOverdue;

    @JsonProperty("fineAmount")
    private Integer fineAmountInCents;

    @Override
    public String getEventType() {
        return "RETURNED";
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
