package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.sqlmapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreSqlEntity;

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