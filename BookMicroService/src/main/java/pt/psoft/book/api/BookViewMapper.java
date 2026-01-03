package pt.psoft.book.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.book.model.query.BookReadModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookViewMapper {

    @Mapping(source = "genreName", target = "genre")
    @Mapping(target = "authors", expression = "java(parseAuthorIds(book.getAuthorIds()))")
    BookView toBookView(BookReadModel book);

    List<BookView> toBookView(List<BookReadModel> books);

    default List<Long> parseAuthorIds(String authorIds) {
        if (authorIds == null || authorIds.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(authorIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}