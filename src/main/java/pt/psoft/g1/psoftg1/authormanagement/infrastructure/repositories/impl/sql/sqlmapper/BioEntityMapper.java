package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql.sqlmapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.BioSqlEntity;

@Mapper(componentModel = "spring")
public interface BioEntityMapper {
    Bio toModel(BioSqlEntity entity);
    BioSqlEntity toEntity(Bio model);

    default Bio map(String value)
    {
        return value == null ? null : new Bio(value);
    }
}
