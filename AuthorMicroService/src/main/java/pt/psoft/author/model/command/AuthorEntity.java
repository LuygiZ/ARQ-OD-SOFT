package pt.psoft.author.model.command;

import jakarta.persistence.*;
import lombok.Getter;
import pt.psoft.author.model.command.valueobjets.AuthorName;
import pt.psoft.author.model.command.valueobjets.Bio;

import java.time.LocalDateTime;

/**
 * Author Entity (Command Model - Write Side)
 * Represents the source of truth for Author aggregate
 */
@Entity
@Table(name = "authors")
@Getter
public class AuthorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_number")
    private Long authorNumber;

    @Embedded
    private AuthorName name;

    @Embedded
    private Bio bio;

    @Column(name = "photo_uri", length = 512)
    private String photoURI;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AuthorEntity() {
        // For JPA
    }

    public AuthorEntity(String name, String bio, String photoURI) {
        this.name = new AuthorName(name);
        this.bio = new Bio(bio);
        this.photoURI = photoURI;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business Methods
    public void update(String name, String bio, String photoURI) {
        if (name != null) {
            this.name = new AuthorName(name);
        }
        if (bio != null) {
            this.bio = new Bio(bio);
        }
        if (photoURI != null) {
            this.photoURI = photoURI;
        }
    }

    public void removePhoto() {
        this.photoURI = null;
    }

    // Getters for Value Objects
    public String getNameValue() {
        return name != null ? name.getValue() : null;
    }

    public String getBioValue() {
        return bio != null ? bio.getValue() : null;
    }
}