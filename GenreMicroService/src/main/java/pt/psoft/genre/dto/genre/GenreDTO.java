package pt.psoft.genre.dto.genre;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Simple Genre DTO for inter-service communication
 * Based on GenreView from monolith
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO implements Serializable {

    private String id;

    @NotNull
    @Size(min = 1, max = 100)
    private String genre;
}