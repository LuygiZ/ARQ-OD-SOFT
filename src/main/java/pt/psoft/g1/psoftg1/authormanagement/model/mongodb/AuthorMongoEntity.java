package pt.psoft.g1.psoftg1.authormanagement.model.mongodb;

import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import pt.psoft.g1.psoftg1.shared.model.mongodb.NameMongoEntity;

@Document(collection = "authors")
public class AuthorMongoEntity extends EntityWithPhoto {

    @Id
    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Getter
    private NameMongoEntity name;

    @Getter
    private BioMongoEntity bio;

    public void setName(NameMongoEntity name) {
        this.name = name;
    }

    public void setBio(BioMongoEntity bio) {
        this.bio = bio;
    }

    public AuthorMongoEntity(NameMongoEntity name, BioMongoEntity bio, String photoURI) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
    }

    protected AuthorMongoEntity() {
        // got ORM only
    }

}
