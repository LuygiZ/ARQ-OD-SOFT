package pt.psoft.genre.genremanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

@Profile("sql-redis")
@Primary
@Entity
@Table(name = "Genre")
public class GenreSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Size(min = 1, max = Genre.GENRE_MAX_LENGTH, message = "Genre name must be between 1 and 100 characters")
    @Column(unique = true, nullable = false, length = Genre.GENRE_MAX_LENGTH)
    @Getter
    private String genre;

    public GenreSqlEntity() { }

    public GenreSqlEntity(String genre) { setGenre(genre); }

    // Getter
    public Long getPk() {
        return pk;
    }

    // Setter
    public void setGenre(String genre) {
        this.genre = genre;
    }
}
