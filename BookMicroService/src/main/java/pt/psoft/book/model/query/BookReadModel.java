package pt.psoft.book.model.query;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Book Read Model (Query Side - CQRS)
 * Fully denormalized for fast queries - NO JOINS needed
 * Synchronized from Command Model via Domain Events
 */
@Entity
@Table(name = "books_read_model", indexes = {
        @Index(name = "idx_book_read_isbn", columnList = "isbn"),
        @Index(name = "idx_book_read_title", columnList = "title"),
        @Index(name = "idx_book_read_genre", columnList = "genre_name"),
        @Index(name = "idx_book_read_authors", columnList = "author_names")
})
@Getter
@Setter
@NoArgsConstructor
public class BookReadModel {

    @Id
    private String isbn;  // Primary key = ISBN for fast lookup

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", length = 4096)
    private String description;

    @Column(name = "genre_name", nullable = false, length = 100)
    private String genreName;  // Denormalized! No JOIN to Genre table

    @Column(name = "author_names", length = 1024)
    private String authorNames;  // Denormalized! Comma-separated author names

    @Column(name = "author_ids", length = 256)
    private String authorIds;  // Comma-separated author IDs for filtering

    @Column(name = "photo_uri", length = 512)
    private String photoURI;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BookReadModel(String isbn,
                         String title,
                         String description,
                         String genreName,
                         String authorNames,
                         String authorIds,
                         String photoURI,
                         Long version) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.genreName = genreName;
        this.authorNames = authorNames;
        this.authorIds = authorIds;
        this.photoURI = photoURI;
        this.version = version;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFromEvent(String title,
                                String description,
                                String genreName,
                                String authorNames,
                                String authorIds,
                                String photoURI,
                                Long version) {
        // Sempre atualizar TODOS os campos (evento representa estado completo)
        this.title = title;
        this.description = description;
        this.genreName = genreName;
        this.authorNames = authorNames;
        this.authorIds = authorIds;
        this.photoURI = photoURI;
        this.version = version;
        this.updatedAt = LocalDateTime.now();
    }
}