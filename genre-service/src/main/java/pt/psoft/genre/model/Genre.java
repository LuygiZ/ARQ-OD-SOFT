package pt.psoft.genre.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.shared.model.BaseEntity;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre extends BaseEntity {

    public static final int GENRE_MAX_LENGTH = 100;

    @Column(unique = true, nullable = false, length = GENRE_MAX_LENGTH)
    @Size(min = 1, max = GENRE_MAX_LENGTH,
            message = "Genre name must be between 1 and 100 characters")
    private String name;

    public Genre(String name) {
        this.name = name;
    }
}