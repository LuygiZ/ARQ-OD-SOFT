package pt.psoft.reader.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import pt.psoft.reader.api.dto.*;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.model.Role;
import pt.psoft.reader.model.User;
import pt.psoft.reader.services.UserService;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * REST API for Authentication operations
 */
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
@Slf4j
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<UserView> login(@RequestBody @Valid AuthRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            String token = generateToken(user, authentication);

            UserView userView = mapToUserView(user);

            log.info("Login successful for user: {}", user.getUsername());

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(userView);

        } catch (BadCredentialsException ex) {
            log.warn("Login failed for user: {} - Bad credentials", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception ex) {
            log.error("Login error for user: {}", request.getUsername(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Register a new reader")
    @PostMapping("/register")
    public ResponseEntity<ReaderView> register(@RequestBody @Valid RegisterReaderRequest request) {
        log.info("Registration attempt for: {}", request.getUsername());

        try {
            Reader reader = userService.createReader(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getBirthDate(),
                    request.getPhoneNumber(),
                    request.getGdprConsent(),
                    request.getMarketingConsent() != null && request.getMarketingConsent(),
                    request.getThirdPartyConsent() != null && request.getThirdPartyConsent()
            );

            ReaderView readerView = mapToReaderView(reader);

            log.info("Registration successful for reader: {} with number: {}",
                     reader.getUsername(), reader.getReaderNumber());

            return ResponseEntity.status(HttpStatus.CREATED).body(readerView);

        } catch (Exception ex) {
            log.error("Registration error for: {}", request.getUsername(), ex);
            throw ex;
        }
    }

    @Operation(summary = "Register a new librarian (for testing)")
    @PostMapping("/register-librarian")
    public ResponseEntity<UserView> registerLibrarian(@RequestBody @Valid AuthRequest request) {
        log.info("Librarian registration attempt for: {}", request.getUsername());

        try {
            User librarian = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getUsername().split("@")[0], // Use email prefix as name
                    Role.LIBRARIAN
            );

            UserView userView = mapToUserView(librarian);

            log.info("Librarian registration successful: {}", librarian.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(userView);

        } catch (Exception ex) {
            log.error("Librarian registration error for: {}", request.getUsername(), ex);
            throw ex;
        }
    }

    private String generateToken(User user, Authentication authentication) {
        Instant now = Instant.now();
        long expirySeconds = 36000L; // 10 hours

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("lms.reader-service")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(String.format("%s,%s", user.getId(), user.getUsername()))
                .claim("roles", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private UserView mapToUserView(User user) {
        return UserView.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(user.getAuthorities().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()))
                .build();
    }

    private ReaderView mapToReaderView(Reader reader) {
        return ReaderView.builder()
                .id(reader.getId())
                .username(reader.getUsername())
                .fullName(reader.getFullName())
                .readerNumber(reader.getReaderNumber())
                .birthDate(reader.getBirthDate())
                .phoneNumber(reader.getPhoneNumber())
                .gdprConsent(reader.isGdprConsent())
                .marketingConsent(reader.isMarketingConsent())
                .thirdPartyConsent(reader.isThirdPartyConsent())
                .roles(reader.getAuthorities().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()))
                .version(reader.getVersion())
                .build();
    }
}
