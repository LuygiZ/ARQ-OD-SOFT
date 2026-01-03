package pt.psoft.book.shared.dto.genre;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Minimal Genre view - just the name
 * Used in other DTOs (e.g., BookDTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreViewDTO implements Serializable {

    @NotNull
    private String genre;
}