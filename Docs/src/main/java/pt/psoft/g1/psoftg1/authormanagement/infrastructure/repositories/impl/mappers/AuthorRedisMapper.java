package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.authormanagement.model.redis.AuthorRedisDTO;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;

@Mapper(componentModel = "spring")
public interface AuthorRedisMapper {

    @Mappings({
            @Mapping(target = "name", expression = "java(author.getName().getName())"),
            @Mapping(target = "bio", expression = "java(author.getBio().getBio())"),
            @Mapping(target = "photoURI", source = "photo", qualifiedByName = "mapPhotoAuthor")
    })
    AuthorRedisDTO toDTO(Author author);

    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "bio", source = "bio")
    })
    Author toDomain(AuthorRedisDTO dto);

    default Name mapName(String name) {
        return new Name(name);
    }

    default Bio mapBio(String bio) {
        return new Bio(bio);
    }

    @Named("mapPhotoAuthor")
    default String mapPhoto(Photo photo) {
        return photo != null ? photo.getPhotoFile() : null;
    }

}