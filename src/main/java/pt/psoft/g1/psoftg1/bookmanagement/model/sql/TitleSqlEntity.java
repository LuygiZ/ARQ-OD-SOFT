package pt.psoft.g1.psoftg1.bookmanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;

import java.io.Serializable;

@Profile("sql")
@Primary
@Embeddable
@EqualsAndHashCode
public class TitleSqlEntity implements Serializable
{
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = Title.TITLE_MAX_LENGTH)
    @Column(name = "TITLE", length = Title.TITLE_MAX_LENGTH, nullable = false)
    @Getter
    private String title;

    public TitleSqlEntity() { }

    public TitleSqlEntity(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}