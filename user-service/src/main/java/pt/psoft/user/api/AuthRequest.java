package pt.psoft.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthRequest {
    @NotNull
    @Email
    private String username;
    @NotNull
    private String password;
}
