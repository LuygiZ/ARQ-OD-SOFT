package pt.psoft.g1.psoftg1.shared.model.mongodb;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Entity
@Table(name="Photo")
public class PhotoMongoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long pk;

    @NotNull
    @Setter
    @Getter
    private String photoFile;

    protected PhotoMongoEntity() {
    }

    public PhotoMongoEntity(Path photoPath) {
        setPhotoFile(photoPath.toString());
    }
}
