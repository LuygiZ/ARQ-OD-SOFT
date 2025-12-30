package pt.psoft.shared.events.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.shared.events.DomainEvent;

@Getter
@NoArgsConstructor
public class UserCreatedEvent extends DomainEvent {
    private String username;
    private boolean success;
    private String failureReason;

    public UserCreatedEvent(String username) {
        super(username, "User");
        this.username = username;
        this.success = true;
    }

    public UserCreatedEvent(String username, String failureReason) {
        super(username, "User");
        this.username = username;
        this.success = false;
        this.failureReason = failureReason;
    }

    @Override
    public String getEventType() {
        return "UserCreated";
    }
}
