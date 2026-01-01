package pt.psoft.reader.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for reader information
 */
@Data
@Builder
public class ReaderView {
    private Long id;
    private String username;
    private String fullName;
    private String readerNumber;
    private LocalDate birthDate;
    private String phoneNumber;
    private boolean gdprConsent;
    private boolean marketingConsent;
    private boolean thirdPartyConsent;
    private Set<String> roles;
    private Long version;
}
