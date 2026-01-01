package pt.psoft.reader.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Response DTO for user information
 */
@Data
@Builder
public class UserView {
    private Long id;
    private String username;
    private String fullName;
    private Set<String> roles;
}
