package pt.psoft.book.bookmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.util.ArrayList;
import java.util.List;

@Profile("sql-redis") //Specifies which environment (or Spring profile) a bean should be loaded in.
@Primary //Marks a bean as the default when multiple candidates of the same type exist.
@Entity
@Table(name = "Book", uniqueConstraints = {
        @UniqueConstraint(name = "uc_book_isbn", columnNames = { "ISBN" })
})
public class BookSqlEntity extends EntityWithPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long pk;

    @Version
    @Getter
    private Long version;

    @Embedded
    private IsbnSqlEntity isbn;

    @Getter
    @Embedded
    @NotNull
    private TitleSqlEntity title;

    @Getter
    @ManyToOne
    @NotNull
    private GenreSqlEntity genre;

    @Getter
    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "BOOK_PK"))
    private List<AuthorSqlEntity> authors = new ArrayList<>();

    @Embedded
    private DescriptionSqlEntity description;

    public BookSqlEntity(IsbnSqlEntity isbn, TitleSqlEntity title, DescriptionSqlEntity description, GenreSqlEntity genre, List<AuthorSqlEntity> authors, String photoURI)
    {
        setTitle(title);
        setIsbn(isbn);
        setDescription(description);
        setAuthors(authors);
        setGenre(genre);
        setPhotoInternal(photoURI);
    }

    protected BookSqlEntity() {}

    // Setters
    private void setTitle(TitleSqlEntity title) { this.title = title; }
    private void setIsbn(IsbnSqlEntity isbn) { this.isbn = isbn; }
    private void setDescription(DescriptionSqlEntity description) { this.description = description; }
    public void setGenre(GenreSqlEntity genre) { this.genre = genre; }
    public void setAuthors(List<AuthorSqlEntity> authors) { this.authors = authors; }

    // Getters
    public String getDescription(){ return this.description.toString(); }
    public String getIsbn(){ return this.isbn.toString(); }
}
