package pt.psoft.author.model.query;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Author Read Model (Query Side - CQRS)
 * Optimized for queries - denormalized and cached
 */
@Entity
@Table(name = "authors_read_model", indexes = {
        @Index(name = "idx_author_read_number", columnList = "author_number"),
        @Index(name = "idx_author_read_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class AuthorReadModel {

    @Id
    @Column(name = "author_number")
    private Long authorNumber;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "bio", nullable = false, length = 4096)
    private String bio;

    @Column(name = "photo_uri", length = 512)
    private String photoURI;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AuthorReadModel(Long authorNumber, String name, String bio, String photoURI, Long version) {
        this.authorNumber = authorNumber;
        this.name = name;
        this.bio = bio;
        this.photoURI = photoURI;
        this.version = version;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFromEvent(String name, String bio, String photoURI, Long version) {
        // Always update ALL fields (event represents complete state)
        this.name = name;
        this.bio = bio;
        this.photoURI = photoURI;
        this.version = version;
        this.updatedAt = LocalDateTime.now();
    }
}