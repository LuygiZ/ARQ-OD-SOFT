package pt.psoft.book.shared.events.genre;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.shared.events.DomainEvent;

@Getter
@NoArgsConstructor // Para Jackson deserialização
public class GenreCreatedEvent extends DomainEvent {

    private String genreId;
    private String genreName;

    public GenreCreatedEvent(String genreId, String genreName) {
        super(genreId, "Genre");
        this.genreId = genreId;
        this.genreName = genreName;
    }

    @Override
    public String getEventType() {
        return "GenreCreated";
    }
}