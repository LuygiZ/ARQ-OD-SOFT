package pt.psoft.genre.api;

import org.springframework.stereotype.Component;
import pt.psoft.genre.model.Genre;
import pt.psoft.shared.dto.genre.GenreDTO;
import pt.psoft.shared.dto.genre.GenreViewDTO;

/**
 * Mapper para converter Genre entity em DTOs
 */
@Component
public class GenreViewMapper {

    /**
     * Converte Genre entity para GenreDTO (com ID)
     */
    public GenreDTO toDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        return new GenreDTO(
                genre.getId(),
                genre.getName()
        );
    }

    /**
     * Converte Genre entity para GenreViewDTO (só nome)
     */
    public GenreViewDTO toViewDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        return new GenreViewDTO(genre.getName());
    }

    /**
     * Converte GenreDTO para Genre entity (para updates)
     */
    public Genre toEntity(GenreDTO dto) {
        if (dto == null) {
            return null;
        }

        Genre genre = new Genre();
        genre.setName(dto.getGenreName());
        return genre;
    }
}