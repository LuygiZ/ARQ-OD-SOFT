package pt.psoft.g1.psoftg1.authormanagement.model.sql;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.shared.model.sql.EntityWithPhotoSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoSqlEntity;

@Profile("sql")
@Primary
@Entity
@Table(name = "AUTHOR")
public class AuthorSqlEntity extends EntityWithPhotoSqlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUTHOR_NUMBER")
    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Embedded
    private NameSqlEntity name;

    @Embedded
    private BioSqlEntity bio;

    protected AuthorSqlEntity() {
    }

    public AuthorSqlEntity(NameSqlEntity name, BioSqlEntity bio, PhotoSqlEntity photoURI) {
        setName(name);
        setBio(bio);
        setPhoto(photoURI);
    }

    // Getters
    public Long getVersion() {
        return version;
    }

    public NameSqlEntity getName() {
        return name;
    }

    public BioSqlEntity getBio() {
        return bio;
    }

    // Setters
    private void setName(NameSqlEntity name) {
        this.name = name;
    }

    private void setBio(BioSqlEntity bio) {
        this.bio = bio;
    }
}