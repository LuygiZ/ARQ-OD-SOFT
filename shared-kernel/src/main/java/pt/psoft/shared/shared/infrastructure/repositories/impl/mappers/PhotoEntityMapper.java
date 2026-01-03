package pt.psoft.shared.shared.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoSqlEntity;

import java.nio.file.Paths;

@Mapper(componentModel = "spring")
public interface PhotoEntityMapper
{
    default Photo toModel(PhotoSqlEntity entity)
    {
        return entity == null ? null : new Photo(Paths.get(entity.getPhotoFile()));
    }

    default PhotoSqlEntity toEntity(Photo model)
    {
        return model == null ? null : new PhotoSqlEntity(model.getPhotoFile().toString());
    }
}
