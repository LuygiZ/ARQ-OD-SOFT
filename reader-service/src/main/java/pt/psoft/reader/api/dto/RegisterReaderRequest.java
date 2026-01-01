package pt.psoft.reader.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for reader registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReaderRequest {

    @NotBlank(message = "Username/email is required")
    @Email(message = "Username must be a valid email")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private LocalDate birthDate;

    private String phoneNumber;

    @NotNull(message = "GDPR consent is required")
    private Boolean gdprConsent;

    private Boolean marketingConsent = false;

    private Boolean thirdPartyConsent = false;
}
