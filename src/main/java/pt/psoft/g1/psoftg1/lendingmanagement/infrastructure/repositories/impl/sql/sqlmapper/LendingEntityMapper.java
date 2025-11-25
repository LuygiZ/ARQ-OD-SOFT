package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql.sqlmapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sql.sqlmapper.BookEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingNumberSqlEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.sql.sqlmapper.ReaderDetailsEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = { ReaderDetailsEntityMapper.class, BookEntityMapper.class})
public interface LendingEntityMapper
{
    Lending toModel(LendingSqlEntity entity);
    LendingSqlEntity toEntity(Lending model);

    default LendingNumber map(LendingNumberSqlEntity entity)
    {
        return entity == null ? null : new LendingNumber(entity.getLendingNumber());
    }

    default LendingNumberSqlEntity map(LendingNumber number)
    {
        return number == null ? null : new LendingNumberSqlEntity(number.getLendingNumber());
    }

    default LendingNumberSqlEntity map(String value)
    {
        return value == null ? null : new LendingNumberSqlEntity(value);
    }

    default Integer map(Optional<Integer> value) {
        return value.orElse(null);
    }

    default Optional<Integer> map(Integer value) {
        return Optional.ofNullable(value);
    }

    default Author map(AuthorSqlEntity value)
    {
        return value == null ? null : new Author(value.getName().toString(), value.getBio().toString(), value.getPhoto().toString());
    }

    default String map(NameSqlEntity value)
    {
        return value == null ? null : value.getName();
    }

    default String map(Bio value)
    {
        return value == null ? null : value.getValue();
    }
}