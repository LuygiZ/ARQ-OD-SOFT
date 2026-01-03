package pt.psoft.book.genremanagement.infrastructure.repositories.impl.mappers;

import org.springframework.stereotype.Component;
import pt.psoft.book.genremanagement.model.Genre;
import pt.psoft.book.genremanagement.model.redis.GenreRedisDTO;

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