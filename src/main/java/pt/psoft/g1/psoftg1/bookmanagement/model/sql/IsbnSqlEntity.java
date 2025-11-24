package pt.psoft.g1.psoftg1.bookmanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.Serializable;

@Profile("sql")
@Primary
@Embeddable
@EqualsAndHashCode
public class IsbnSqlEntity implements Serializable {

    @NotNull
    @Size(min = 10, max = 13)
    @Column(name = "ISBN", length = 16, unique = true, nullable = false)
    private String isbn;

    public IsbnSqlEntity(String isbn)
    {
        setIsbn(isbn);
    }

    protected IsbnSqlEntity() {}

    // Getters
    public String getIsbn()
    {
        return isbn;
    }

    // Setters
    private void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return isbn; // MapStruct muitas vezes chama toString() se o getter falhar
    }
}
