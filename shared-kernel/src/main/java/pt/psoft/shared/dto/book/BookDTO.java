package pt.psoft.shared.dto.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for Book inter-service communication
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
    private String genre;  // Genre name

    @JsonProperty("authorIds")
    private List<Long> authorIds;  // Author numbers

    @JsonProperty("photoURI")
    private String photoURI;

    @JsonProperty("version")
    private Long version;
}
