package pt.psoft.book.model.command;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Book Entity (Write Model)
 * Follows BookSqlEntity pattern from monolith but with denormalized relationships
 */
@Entity
@Table(name = "books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_book_isbn", columnNames = {"isbn"})
})
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long pk;

    @Version
    @Getter
    private Long version;

    @Embedded
    @Getter
    private Isbn isbn;

    @Embedded
    @Getter
    private Title title;

    @Embedded
    @Getter
    private Description description;

    // Denormalized: Genre name instead of FK to Genre table
    @Column(name = "genre_name", nullable = false, length = 100)
    @Getter
    private String genreName;

    // Denormalized: Author IDs instead of ManyToMany join table
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_command_authors", joinColumns = @JoinColumn(name = "book_pk"))
    @Column(name = "author_id")
    @Getter
    private List<Long> authorIds = new ArrayList<>();

    @Column(name = "photo_uri", length = 512)
    @Getter
    private String photoURI;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Getter
    private LocalDateTime updatedAt;

    protected BookEntity() {
        // JPA
    }

    public BookEntity(String isbn,
                             String title,
                             String description,
                             String genreName,
                             List<Long> authorIds,
                             String photoURI) {

        this.isbn = new Isbn(isbn);
        this.title = new Title(title);
        this.description = new Description(description);
        setGenreName(genreName);
        setAuthorIds(authorIds);
        this.photoURI = photoURI;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business Logic

    public void updateTitle(String newTitle) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = new Title(newTitle);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateDescription(String newDescription) {
        this.description = new Description(newDescription);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateGenre(String newGenreName) {
        setGenreName(newGenreName);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAuthors(List<Long> newAuthorIds) {
        setAuthorIds(newAuthorIds);
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePhoto(String newPhotoURI) {
        this.photoURI = newPhotoURI;
        this.updatedAt = LocalDateTime.now();
    }

    public void removePhoto() {
        this.photoURI = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title,
                       String description,
                       String genreName,
                       List<Long> authorIds,
                       String photoURI) {

        if (title != null) updateTitle(title);
        if (description != null) updateDescription(description);
        if (genreName != null) updateGenre(genreName);
        if (authorIds != null) updateAuthors(authorIds);
        if (photoURI != null) updatePhoto(photoURI);
    }

    // Getters for embedded values
    public String getIsbnValue() {
        return isbn != null ? isbn.getIsbn() : null;
    }

    public String getTitleValue() {
        return title != null ? title.getTitle() : null;
    }

    public String getDescriptionValue() {
        return description != null ? description.getDescription() : null;
    }

    // Invariants

    private void setGenreName(String genreName) {
        if (genreName == null || genreName.isBlank()) {
            throw new IllegalArgumentException("Genre cannot be null or empty");
        }
        this.genreName = genreName.trim();
    }

    private void setAuthorIds(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("Book must have at least one author");
        }
        this.authorIds = new ArrayList<>(authorIds);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}