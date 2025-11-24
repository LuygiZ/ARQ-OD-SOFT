package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sqlmapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sqlmapper.AuthorEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.DescriptionSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.IsbnSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.TitleSqlEntity;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.sqlmapper.GenreEntityMapper;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sqlmapper.PhotoEntityMapper;

@Mapper(componentModel = "spring", uses = {GenreEntityMapper.class, AuthorEntityMapper.class, PhotoEntityMapper.class})
public interface BookEntityMapper {

    // Entity to Model mapping
    @Mapping(target = "photoURI", source = "photo")
    Book toModel(BookSqlEntity entity);

    // Model to Entity mapping
    BookSqlEntity toEntity(Book model);

    // Title entity conversion
    default String map(TitleSqlEntity titleEntity) {
        return titleEntity == null ? null : titleEntity.getTitle();
    }

    // ISBN entity conversion
    default String map(IsbnSqlEntity isbnEntity) {
        return isbnEntity == null ? null : isbnEntity.getIsbn();
    }

    // Description entity conversion
    default String map(DescriptionSqlEntity descEntity) {
        return descEntity == null ? null : descEntity.getDescription();
    }
}