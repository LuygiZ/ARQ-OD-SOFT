package pt.psoft.g1.psoftg1.authormanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;

@Profile("sql")
@Primary
@Embeddable
public class BioSqlEntity
{
    @Column(nullable = false, length = Bio.BIO_MAX_LENGTH)
    @NotNull
    @Size(min = 1, max = Bio.BIO_MAX_LENGTH)
    @Getter
    private String bio;

    protected BioSqlEntity() { }

    public BioSqlEntity(String bio)
    {
        setBio(bio);
    }

    // Setters
    public void setBio(String bio) { this.bio = bio; }
}
