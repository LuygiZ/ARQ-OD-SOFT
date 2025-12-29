package pt.psoft.shared.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;
import pt.psoft.shared.events.genre.GenreCreatedEvent;
import pt.psoft.shared.events.genre.GenreDeletedEvent;
import pt.psoft.shared.events.genre.GenreUpdatedEvent;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
        // Genre Events
        @JsonSubTypes.Type(value = GenreCreatedEvent.class, name = "GenreCreated"),
        @JsonSubTypes.Type(value = GenreUpdatedEvent.class, name = "GenreUpdated"),
        @JsonSubTypes.Type(value = GenreDeletedEvent.class, name = "GenreDeleted"),
        // Book Events
        @JsonSubTypes.Type(value = BookCreatedEvent.class, name = "BookCreated"),
        @JsonSubTypes.Type(value = BookUpdatedEvent.class, name = "BookUpdated"),
        @JsonSubTypes.Type(value = BookDeletedEvent.class, name = "BookDeleted"),
        // Author Events
        @JsonSubTypes.Type(value = AuthorCreatedEvent.class, name = "AuthorCreated"),
        @JsonSubTypes.Type(value = AuthorUpdatedEvent.class, name = "AuthorUpdated"),
        @JsonSubTypes.Type(value = AuthorDeletedEvent.class, name = "AuthorDeleted")
})
public abstract class DomainEvent implements Serializable {

    private String eventId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String aggregateId;
    private String aggregateType;

    // Construtor protegido no-args para Jackson/Lombok
    protected DomainEvent() {
        // Jackson precisa deste construtor vazio
    }

    // Construtor com parâmetros (usado pelas subclasses)
    protected DomainEvent(String aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public abstract String getEventType();
}