package pt.psoft.g1.psoftg1.authormanagement.model.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class BioMongoEntity {

    @Id
    @Getter
    @Setter
    private String bioId;

    private final int BIO_MAX_LENGTH = 4096;

    @NotNull
    @Size(min = 1, max = BIO_MAX_LENGTH)
    @Field("bio")
    @Getter
    private String bio;

    public BioMongoEntity(String bio) {
        setBio(bio);
    }

    protected BioMongoEntity() {
        // for ORM only
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return bio;
    }
}
