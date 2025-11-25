package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.BioSqlEntity;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.mappers.PhotoEntityMapper;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.mappers.NameEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoSqlEntity;

@Mapper(componentModel = "spring", uses = { NameEntityMapper.class, BioEntityMapper.class, PhotoEntityMapper.class})
public interface AuthorEntityMapper
{
    Author toModel(AuthorSqlEntity entity);
    AuthorSqlEntity toEntity(Author model);

    // Mapping methods for Photo (Entity -> String)
    default String map(PhotoSqlEntity entity) {
        return entity == null ? null : entity.getPhotoFile();
    }

    // Mapping methods for Photo (String -> Entity)
    default PhotoSqlEntity map(String photoFile) {
        return photoFile == null ? null : new PhotoSqlEntity(photoFile);
    }

    // Mapping methods for Name (Entity -> String)
    default String map(NameSqlEntity entity) {
        return entity == null ? null : entity.getName();
    }

    // Mapping methods for Name (String -> Entity)
    default NameSqlEntity mapToName(String name) {
        return name == null ? null : new NameSqlEntity(name);
    }

    // Mapping methods for Bio (Entity -> String)
    default String map(BioSqlEntity entity) {
        return entity == null ? null : entity.getBio();
    }

    // Mapping methods for Bio (String -> Entity)
    default BioSqlEntity mapToBio(String bio) {
        return bio == null ? null : new BioSqlEntity(bio);
    }
}