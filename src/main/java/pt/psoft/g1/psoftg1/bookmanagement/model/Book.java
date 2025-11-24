package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa um livro no sistema de gestão de biblioteca.
 */
public class Book extends EntityWithPhoto
{
    // TODO: Substituir por ID e nao é suposto ser public
    public Long pk;
    private Long version;
    private Isbn isbn;
    private Title title;
    private Description description;
    private Genre genre;
    private List<Author> authors;

    public Book(String isbn, String title, String description, Genre genre, List<Author> authors, String photoURI)
    {
        setTitle(new Title(title));
        setIsbn(new Isbn(isbn));
        setGenre(genre);
        setAuthors(authors);
        setPhotoInternal(photoURI);
        setDescription(new Description(description));

        this.version = 0L;
    }

    // Getters
    public Long getPk() { return pk; }
    public Isbn getIsbn() { return isbn; }
    public Title getTitle() { return title; }
    public Description getDescription() { return description; }
    public Genre getGenre() { return genre; }
    public List<Author> getAuthors() { return authors; }
    public Long getVersion() { return version; }

    // Setters
    public void setTitle(Title title) {this.title = title;}
    public void setIsbn(Isbn isbn) { this.isbn = isbn;}
    public void setDescription(Description description) {this.description = description; }
    public void setGenre(Genre genre)
    {
        if(genre == null)
        {
            throw new IllegalArgumentException("Genre cannot be null");
        }

        this.genre = genre;
    }
    public void setAuthors(List<Author> authors)
    {
        if(authors == null || authors.isEmpty())
        {
            throw new IllegalArgumentException("Authors cannot be empty");
        }

        this.authors = authors;
    }

    // regras de negócio
    public void applyPatch(final Long expectedVersion, UpdateBookRequest request)
    {
        if (!Objects.equals(this.version, expectedVersion))
        {
            throw new StaleObjectStateException("Object was already modified by another user", this.pk);
        }

        String title = request.getTitle();
        String description = request.getDescription();
        Genre genre = request.getGenreObj();
        List<Author> authors = request.getAuthorObjList();
        String photoURI = request.getPhotoURI();
        if(title != null)
        {
            setTitle(new Title(title));
        }

        if(description != null)
        {
            setDescription(new Description(description));
        }

        if(genre != null)
        {
            setGenre(genre);
        }

        if(authors != null)
        {
            setAuthors(authors);
        }

        if(photoURI != null)
        {
            setPhotoInternal(photoURI);
        }
    }

    public void removePhoto(Long expectedVersion)
    {
        if(!Objects.equals(this.version, expectedVersion))
        {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        setPhotoInternal((String)null);
    }
}