package pt.psoft.book.genremanagement.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import pt.psoft.book.genremanagement.model.Genre;
import pt.psoft.book.genremanagement.model.sql.GenreSqlEntity;

@Mapper(componentModel = "spring")
public interface GenreEntityMapper
{
    Genre toModel(GenreSqlEntity entity);
    GenreSqlEntity toEntity(Genre model);

    default String map(GenreSqlEntity entity) {
        return entity == null ? null : entity.getGenre();
    }

    default Genre map(String genre) {
        return genre == null ? null : new Genre(genre);
    }
}