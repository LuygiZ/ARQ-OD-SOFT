package pt.psoft.bookquery.model;

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
    private String isbn;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", length = 4096)
    private String description;

    @Column(name = "genre_name", nullable = false, length = 100)
    private String genreName;

    @Column(name = "author_names", length = 1024)
    private String authorNames;

    @Column(name = "author_ids", length = 256)
    private String authorIds;

    @Column(name = "photo_uri", length = 512)
    private String photoURI;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

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
        this.title = title;
        this.description = description;
        this.genreName = genreName;
        this.authorNames = authorNames;
        this.authorIds = authorIds;
        this.photoURI = photoURI;
        this.version = version;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Double averageRating, Integer totalReviews) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0;
        this.updatedAt = LocalDateTime.now();
    }
}
