package pt.psoft.g1.psoftg1.authormanagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql.sqlmapper.BioEntityMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sql.sqlmapper.NameEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.Name;

@Mapper(componentModel = "spring", uses = {BioEntityMapper.class, NameEntityMapper.class})
public abstract class AuthorMapper extends MapperInterface {

    @Mapping(target = "authorNumber", ignore = true)  // Assuming authorNumber is auto-generated
    public abstract Author create(CreateAuthorRequest request);

    @Mapping(target = "authorNumber", ignore = true)  // Don't update authorNumber
    public abstract void update(UpdateAuthorRequest request, @MappingTarget Author author);

    // Add this mapping method for String -> Name conversion
    protected Name map(String name) {
        return name == null ? null : new Name(name);
    }
}
