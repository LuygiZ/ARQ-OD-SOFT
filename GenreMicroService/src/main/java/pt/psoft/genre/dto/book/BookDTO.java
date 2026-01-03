package pt.psoft.genre.dto.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * No longer needs authorNames field since BookView.authors
 * now returns List<Long> (IDs) instead of List<String> (names)
 *
 * Feign Client can now map BookView.authors → BookDTO.authorIds directly
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO implements Serializable {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("genre")
    private String genre;

    /**
     * Author IDs - mapped from BookView.authors (List<Long>)
     */
    @JsonProperty("authors")  // ✅ Maps to BookView.authors
    private List<Long> authorIds;

    @JsonProperty("photoURI")
    private String photoURI;

    @JsonProperty("version")
    private Long version;
}