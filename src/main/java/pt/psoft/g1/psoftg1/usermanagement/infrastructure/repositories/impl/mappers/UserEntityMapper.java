package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.LibrarianSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.ReaderSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserSqlEntity;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserEntityMapper
{
    User toModel(UserSqlEntity entity);
    UserSqlEntity toEntity(User model);

    Librarian toModel(LibrarianSqlEntity entity);
    @Mapping(target = "role", source="authorities")
    LibrarianSqlEntity toEntity(Librarian user);

    Reader toModel(ReaderSqlEntity entity);
    @Mapping(target = "role", source="authorities")
    ReaderSqlEntity toEntity(Reader user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "role", source="authorities")
    ReaderSqlEntity toReaderEntity(UserSqlEntity userEntity);

    // Factory methods para criar instâncias sem encriptar
    @ObjectFactory
    default User createUser(UserSqlEntity entity) {
        return User.forMapper(entity.getUsername(), entity.getPassword());
    }

    @ObjectFactory
    default Reader createReader(ReaderSqlEntity entity) {
        return Reader.forMapper(entity.getUsername(), entity.getPassword());
    }

    @ObjectFactory
    default Librarian createLibrarian(LibrarianSqlEntity entity) {
        return Librarian.forMapper(entity.getUsername(), entity.getPassword());
    }

    default String map(NameSqlEntity value)
    {
        return value == null ? null : value.toString();
    }

    default Role map(Set<Role> value)
    {
        if (value == null || value.isEmpty())
        {
            return null;
        }
        return value.iterator().next();
    }
}