package pt.psoft.genre.genremanagement.infrastructure.repositories.impl.mappers;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.redis.GenreRedisDTO;

@Component
public class GenreRedisMapper {

    public GenreRedisDTO toDTO(Genre genre) {
        return new GenreRedisDTO(
                genre.getPk(),
                genre.getGenre()
        );
    }

    public Genre toDomain(GenreRedisDTO dto) {
        Genre genre = new Genre(dto.getGenre());
        genre.pk = dto.getPk();
        return genre;
    }
}