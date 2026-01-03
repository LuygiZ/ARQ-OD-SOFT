package pt.psoft.genre.events.genre;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.genre.events.DomainEvent;

@Getter
@NoArgsConstructor
public class GenreDeletedEvent extends DomainEvent {

    private String genreId;
    private String genreName;

    public GenreDeletedEvent(String genreId, String genreName) {
        super(genreId, "Genre");
        this.genreId = genreId;
        this.genreName = genreName;
    }

    @Override
    public String getEventType() {
        return "GenreDeleted";
    }
}