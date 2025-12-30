package pt.psoft.shared.events.reader;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.shared.events.DomainEvent;

@Getter
@NoArgsConstructor
public class ReaderCreatedEvent extends DomainEvent {
    private String username;
    private String password;
    private String fullName;

    public ReaderCreatedEvent(String username, String password, String fullName) {
        super(username, "Reader");
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public String getEventType() {
        return "ReaderCreated";
    }
}
