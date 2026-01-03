package pt.psoft.genre.shared.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;

@Mapper(componentModel = "spring")
public interface NameEntityMapper
{
    Name toModel(NameSqlEntity entity);
    NameSqlEntity toEntity(Name model);
}