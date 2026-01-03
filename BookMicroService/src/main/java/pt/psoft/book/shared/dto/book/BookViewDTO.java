package pt.psoft.book.shared.dto.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for Book API responses (Read Model)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookViewDTO implements Serializable {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("genre")
    private String genre;  // Denormalized genre name

    @JsonProperty("authors")
    private List<String> authors;  // Denormalized author names

    @JsonProperty("photoURI")
    private String photoURI;

    @JsonProperty("version")
    private Long version;
}
