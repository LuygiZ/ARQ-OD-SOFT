package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sqlmapper;

import org.mapstruct.Mapper;

import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.sql.ForbiddenNameSqlEntity;

@Mapper(componentModel = "spring")
public interface ForbiddenNameEntityMapper
{
    ForbiddenName toModel(ForbiddenNameSqlEntity entity);
    ForbiddenNameSqlEntity toEntity(ForbiddenName model);
}
