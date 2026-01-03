package pt.psoft.book.bookmanagement.model.redis;

import java.io.Serializable;
import java.util.List;

public class BookRedisDTO implements Serializable {
    private Long pk;
    private Long version;
    private String isbn;
    private String title;
    private String description;
    private String genre;
    private List<Long> authorNumbers;
    private String photoURI;

    public BookRedisDTO() {

    }

    public BookRedisDTO(Long pk, Long version, String isbn, String title, String description,
                        String genre, List<Long> authorNumbers, String photoURI) {
        this.pk = pk;
        this.version = version;
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.authorNumbers = authorNumbers;
        this.photoURI = photoURI;
    }

    // Getters e Setters
    public Long getPk() { return pk; }
    public void setPk(Long pk) { this.pk = pk; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public List<Long> getAuthorNumbers() { return authorNumbers; }
    public void setAuthorNumbers(List<Long> authorNumbers) { this.authorNumbers = authorNumbers; }

    public String getPhotoURI() { return photoURI; }
    public void setPhotoURI(String photoURI) { this.photoURI = photoURI; }
}