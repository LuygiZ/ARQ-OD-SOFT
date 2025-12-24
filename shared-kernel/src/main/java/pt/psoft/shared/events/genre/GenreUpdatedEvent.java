package pt.psoft.shared.events.genre;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.shared.events.DomainEvent;

@Getter
@NoArgsConstructor
public class GenreUpdatedEvent extends DomainEvent {

    private String genreId;
    private String genreName;

    public GenreUpdatedEvent(String genreId, String genreName) {
        super(genreId, "Genre");
        this.genreId = genreId;
        this.genreName = genreName;
    }

    @Override
    public String getEventType() {
        return "GenreUpdated";
    }
}