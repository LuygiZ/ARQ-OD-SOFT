package pt.psoft.g1.psoftg1.bookmanagement.model.mongodb;

import jakarta.validation.constraints.NotNull;

import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;

import pt.psoft.g1.psoftg1.authormanagement.model.mongodb.AuthorMongoEntity;
import pt.psoft.g1.psoftg1.genremanagement.model.mongodb.GenreMongoEntity;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.util.ArrayList;
import java.util.List;

@Profile("mongodb")
@Document(collection = "books")
public class BookMongoEntity extends EntityWithPhoto {

    @Id
    private String bookId;

    @Version
    @Getter
    private Long version;

    @Field("isbn")
    @Indexed(unique = true)
    private IsbnMongoEntity isbn;

    @Getter
    @Field("title")
    @NotNull
    private TitleMongoEntity title;

    @Getter
    @DBRef // foreign key reference
    @NotNull
    @Field("genre")
    private GenreMongoEntity genre;

    @Getter
    @DBRef // foreign key reference
    @Field("authors")
    private List<AuthorMongoEntity> authors = new ArrayList<>();

    @Field("description")
    private DescriptionMongoEntity description;

    public BookMongoEntity(
            IsbnMongoEntity isbn,
            TitleMongoEntity title,
            DescriptionMongoEntity description,
            GenreMongoEntity genre,
            List<AuthorMongoEntity> authors,
            String photoURI) {

        setTitle(title);
        setIsbn(isbn);
        setDescription(description);
        setGenre(genre);
        setAuthors(authors);
        setPhotoInternal(photoURI);
    }

    protected BookMongoEntity() {
        // got ORM only
    }

    private void setTitle(TitleMongoEntity title) {
        this.title = title;
    }

    private void setIsbn(IsbnMongoEntity isbn) {
        this.isbn = isbn;
    }

    private void setDescription(DescriptionMongoEntity description) {
        this.description = description;
    }

    private void setGenre(GenreMongoEntity genre) {
        this.genre = genre;
    }

    private void setAuthors(List<AuthorMongoEntity> authors) {
        this.authors = authors;
    }

    public String getIsbn() {
        return this.isbn.toString();
    }

    public String getDescription() {
        return this.description.toString();
    }
}
