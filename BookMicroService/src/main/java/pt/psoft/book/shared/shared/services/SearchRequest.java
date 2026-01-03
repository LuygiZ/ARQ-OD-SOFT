package pt.psoft.book.shared.shared.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.psoft.shared.shared.services.Page;

/**
 * Based on <a href="https://github.com/Yoh0xFF/java-spring-security-example">https://github.com/Yoh0xFF/java-spring-security-example</a>
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchRequest<T> {
    @Valid
    @NotNull
    Page page;

    @Valid
    @NotNull
    T query;
}