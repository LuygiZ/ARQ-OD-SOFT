package pt.psoft.shared.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Author inter-service communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO implements Serializable {

    @JsonProperty("authorNumber")
    private Long authorNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("photoURI")
    private String photoURI;

    @JsonProperty("version")
    private Long version;
}