package pt.psoft.author.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Read Model for Author - Stored in MongoDB
 * This is the Query side of CQRS implementing Polyglot Persistence
 *
 * PostgreSQL → Command Model (writes/transactions)
 * MongoDB → Read Model (reads/queries)
 */
@Document(collection = "authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorReadModel {

    @Id
    private String id; // MongoDB ObjectId

    @Field("author_number")
    @Indexed(unique = true)
    private Long authorNumber; // Business key from PostgreSQL

    @Field("name")
    @Indexed
    private String name;

    @Field("bio")
    private String bio;

    @Field("photo_uri")
    private String photoURI;

    @Field("version")
    private Long version;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Update from Event
     */
    public void updateFromEvent(Long authorNumber, String name, String bio,
                                String photoURI, Long version,
                                LocalDateTime updatedAt) {
        this.authorNumber = authorNumber;
        this.name = name;
        this.bio = bio;
        this.photoURI = photoURI;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    // Getters for compatibility with existing code
    public Long getAuthorNumber() {
        return authorNumber;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}