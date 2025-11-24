package pt.psoft.g1.psoftg1.bookmanagement.model.sql;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.bookmanagement.model.Description;
import pt.psoft.g1.psoftg1.shared.model.StringUtilsCustom;

import java.io.Serializable;

@Profile("sql")
@Primary
@Embeddable
public class DescriptionSqlEntity implements Serializable
{
    @Size(max = Description.DESC_MAX_LENGTH)
    @Column(length = Description.DESC_MAX_LENGTH)
    private String description;

    public DescriptionSqlEntity(String description)
    {
        setDescription(description);
    }

    public DescriptionSqlEntity() {}

    // Getters
    public String getDescription()
    {
        return description;
    }

    // Setters
    public void setDescription(String description)
    {
        this.description = description;
    }
}
