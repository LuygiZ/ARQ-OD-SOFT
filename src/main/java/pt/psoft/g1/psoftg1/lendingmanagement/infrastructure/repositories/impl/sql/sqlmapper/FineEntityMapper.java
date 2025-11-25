package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql.sqlmapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.FineSqlEntity;

@Mapper(componentModel = "spring", uses = {LendingEntityMapper.class})
public interface FineEntityMapper
{
    Fine toModel(FineSqlEntity entity);
    FineSqlEntity toEntity(Fine model);

    default String map(Bio value)
    {
        return value == null ? null : value.getValue();
    }
}