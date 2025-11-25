package pt.psoft.g1.psoftg1.genremanagement.model.mongodb;

import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Profile("mongodb")
@Document(collection = "genres")
public class GenreMongoEntity {

    private final int GENRE_MAX_LENGTH = 100;
    @Id
    String genreId;

    @Size(min = 1, max = GENRE_MAX_LENGTH, message = "Genre name must be between 1 and 100 characters")
    @Getter
    @Field("genre")
    String genre;

    public GenreMongoEntity(String genre) {
        setGenre(genre);
    }

    protected GenreMongoEntity() {
        // for ORM only
    }

    private void setGenre(String genre) {
        if (genre == null)
            throw new IllegalArgumentException("Genre cannot be null");
        if (genre.isBlank())
            throw new IllegalArgumentException("Genre cannot be blank");
        if (genre.length() > GENRE_MAX_LENGTH)
            throw new IllegalArgumentException("Genre has a maximum of 4096 characters");
        this.genre = genre;
    }

    public String toString() {
        return genre;
    }
}
