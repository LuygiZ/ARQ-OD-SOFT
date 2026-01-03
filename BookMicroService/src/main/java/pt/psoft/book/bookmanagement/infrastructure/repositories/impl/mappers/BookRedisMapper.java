package pt.psoft.book.bookmanagement.infrastructure.repositories.impl.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql.AuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.*;
import pt.psoft.g1.psoftg1.bookmanagement.model.redis.BookRedisDTO;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.sql.GenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookRedisMapper {

    private final GenreRepositoryImpl genreRepo;
    private final AuthorRepositoryImpl authorRepo;

    public BookRedisDTO toDTO(Book book) {
        List<Long> authorNumbers = book.getAuthors().stream()
                .map(Author::getAuthorNumber)
                .collect(Collectors.toList());

        String photoURI = book.getPhoto() != null ? book.getPhoto().getPhotoFile() : null;

        return new BookRedisDTO(
                book.getPk(),
                book.getVersion(),
                book.getIsbn().toString(),
                book.getTitle().toString(),
                book.getDescription().toString(),
                book.getGenre().getGenre(),
                authorNumbers,
                photoURI
        );
    }

    public Book toDomain(BookRedisDTO dto) {
        // Buscar Genre
        Genre genre = genreRepo.findByString(dto.getGenre())
                .orElseThrow(() -> new RuntimeException("Genre not found: " + dto.getGenre()));

        // Buscar Authors
        List<Author> authors = new ArrayList<>();
        for (Long authorNumber : dto.getAuthorNumbers()) {
            Author author = authorRepo.findByAuthorNumber(authorNumber)
                    .orElseThrow(() -> new RuntimeException("Author not found: " + authorNumber));
            authors.add(author);
        }

        // Criar Book
        Book book = new Book(
                dto.getIsbn(),
                dto.getTitle(),
                dto.getDescription(),
                genre,
                authors,
                dto.getPhotoURI()
        );

        // Set pk (usa reflection ou adiciona setter temporário)
        book.pk = dto.getPk();

        return book;
    }
}