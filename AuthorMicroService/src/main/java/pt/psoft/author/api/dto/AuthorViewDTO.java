package pt.psoft.author.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for Author Read Model (Query Side)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorViewDTO implements Serializable {

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

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}