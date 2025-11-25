package pt.psoft.g1.psoftg1.bookmanagement.model.mongodb;

import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode
@Document(collection = "isbns")
public class IsbnMongoEntity implements Serializable {

    @Id
    private String isbnId;

    @Size(min = 10, max = 13)
    @Field("isbn")
    private String isbn;

    public IsbnMongoEntity(String isbn) {
        setIsbn(isbn);
    }

    protected IsbnMongoEntity() {
    };

    public String toString() {
        return this.isbn;
    }

    private void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
