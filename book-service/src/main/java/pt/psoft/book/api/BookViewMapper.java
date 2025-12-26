package pt.psoft.book.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.book.model.query.BookReadModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookViewMapper {

    @Mapping(target = "genre", source = "genreName")
    @Mapping(target = "authors", expression = "java(parseAuthorNames(book.getAuthorNames()))")
    BookView toBookView(BookReadModel book);

    List<BookView> toBookView(List<BookReadModel> books);

    default List<String> parseAuthorNames(String authorNames) {
        if (authorNames == null || authorNames.isBlank()) {
            return List.of();
        }
        return Arrays.stream(authorNames.split(",\\s*"))
                .collect(Collectors.toList());
    }
}