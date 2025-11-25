package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql.AuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sql.sqlmapper.BookEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.sql.GenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreSqlEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository
{
    private final SpringDataBookRepository bookRepo;
    private final BookEntityMapper bookEntityMapper;
    private final GenreRepositoryImpl genreRepo;
    private final AuthorRepositoryImpl authorRepo;
    private final EntityManager em;

    @Override
    public List<Book> findByGenre(@Param("genre") String genre)
    {
        List<Book> books = new ArrayList<>();
        for (BookSqlEntity b: bookRepo.findByGenre(genre))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> findByTitle(@Param("title") String title)
    {
        List<Book> books = new ArrayList<>();
        for (BookSqlEntity b: bookRepo.findByTitle(title))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> findByAuthorName(@Param("authorName") String authorName)
    {
        List<Book> books = new ArrayList<>();
        for (BookSqlEntity b: bookRepo.findByAuthorName(authorName))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public Optional<Book> findByIsbn(@Param("isbn") String isbn)
    {
        Optional<BookSqlEntity> entityOpt = bookRepo.findByIsbn(isbn);
        if(entityOpt.isPresent())
        {
            return Optional.of(bookEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable)
    {
        //TODO: Corrigir este
        return bookRepo.findTop5BooksLent(oneYearAgo, pageable);
    }

    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber)
    {
        List<Book> books = new ArrayList<>();
        for (BookSqlEntity b: bookRepo.findBooksByAuthorNumber(authorNumber))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query)
    {
        String title = query.getTitle();
        String genre = query.getGenre();
        String authorName = query.getAuthorName();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<BookSqlEntity> cq = cb.createQuery(BookSqlEntity.class);
        final Root<BookSqlEntity> root = cq.from(BookSqlEntity.class);
        final Join<BookSqlEntity, Genre> genreJoin = root.join("genre");
        final Join<BookSqlEntity, Author> authorJoin = root.join("authors");
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(title))
            where.add(cb.like(root.get("title").get("title"), title + "%"));

        if (StringUtils.hasText(genre))
            where.add(cb.like(genreJoin.get("genre"), genre + "%"));

        if (StringUtils.hasText(authorName))
            where.add(cb.like(authorJoin.get("name").get("name"), authorName + "%"));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("title"))); // Order by title, alphabetically

        final TypedQuery<BookSqlEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List <Book> books = new ArrayList<>();

        for (BookSqlEntity bookEntity : q.getResultList()) {
            books.add(bookEntityMapper.toModel(bookEntity));
        }

        return books;
    }

    @Override
    @Transactional
    public Book save(Book book)
    {
        // Convert the domain model (Book) to a JPA entity (BookEntity)
        BookSqlEntity entity = bookEntityMapper.toEntity(book);

        // Retrieve the existing Genre model from the repository
        // Throws an exception if the genre is not found
        Genre genreModel = genreRepo.findByString(book.getGenre().getGenre())
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        // Get the managed JPA reference for the GenreEntity using its database ID (pk)
        // This ensures we use the existing GenreEntity instead of creating a new one
        GenreSqlEntity genreEntity = em.getReference(GenreSqlEntity.class, genreModel.getPk());

        // Set the managed GenreEntity on the BookEntity
        entity.setGenre(genreEntity);

        // Prepare a list to hold managed AuthorEntity instances
        List<AuthorSqlEntity> authors = new ArrayList<>();

        // For each author in the Book model
        for (var author : book.getAuthors())
        {
            // Retrieve the corresponding Author model from the repository by author number
            //TODO: temos aqui uma questao, o searchByNameName retorna uma lista de nomes, entao pode nao ser o autor correto (no caso de haver varios autores com o mesmo nome)
            Author auth  = authorRepo.searchByNameName(author.getName().getName()).get(0);
            if (auth == null)
            {
                throw new RuntimeException("Author not found");
            }

            // Get a managed reference to the existing AuthorEntity by its author number
            AuthorSqlEntity authorEntity = em.getReference(AuthorSqlEntity.class, auth.getAuthorNumber());

            // Add the managed AuthorEntity to the list
            authors.add(authorEntity);
        }

        // Associate all managed AuthorEntity objects with the BookEntity
        entity.setAuthors(authors);

        // Persist the BookEntity and return the saved Book as a domain model
        BookSqlEntity saved = bookRepo.save(entity);
        System.out.println("Saved entity ISBN: " +
                (saved.getIsbn() != null ? saved.getIsbn() : "null"));
        return bookEntityMapper.toModel(saved);
    }

    @Override
    public void delete(Book book)
    {
        // TODO: implement delete logic
    }

    // Adiciona este método público
    public Optional<BookSqlEntity> findSqlEntityByIsbn(String isbn) {
        return bookRepo.findByIsbn(isbn);
    }
}
