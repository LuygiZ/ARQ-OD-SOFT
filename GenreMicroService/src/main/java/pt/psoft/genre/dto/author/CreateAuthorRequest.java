package pt.psoft.genre.dto.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for creating a new Author
 * Shared across all services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuthorRequest implements Serializable {

    @NotBlank(message = "Name is mandatory")
    @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters")
    private String name;

    @NotBlank(message = "Bio is mandatory")
    @Size(min = 1, max = 4096, message = "Bio must be between 1 and 4096 characters")
    private String bio;

    @Size(max = 512, message = "Photo URI cannot exceed 512 characters")
    private String photoURI;
}