package pt.psoft.reader.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.reader.model.Librarian;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.model.Role;
import pt.psoft.reader.model.User;
import pt.psoft.reader.repositories.ReaderRepository;
import pt.psoft.reader.repositories.UserRepository;
import pt.psoft.shared.exceptions.ConflictException;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ReaderRepository readerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User createUser(String username, String password, String fullName, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }

        User user;
        switch (role) {
            case Role.READER -> {
                String readerNumber = generateReaderNumber();
                user = Reader.newReader(username, password, fullName, readerNumber);
            }
            case Role.LIBRARIAN -> user = Librarian.newLibrarian(username, password, fullName);
            default -> user = User.newUser(username, password, fullName, role);
        }

        return userRepository.save(user);
    }

    @Transactional
    public Reader createReader(String username, String password, String fullName,
                               LocalDate birthDate, String phoneNumber,
                               boolean gdprConsent, boolean marketingConsent, boolean thirdPartyConsent) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }

        if (!gdprConsent) {
            throw new IllegalArgumentException("GDPR consent is required");
        }

        String readerNumber = generateReaderNumber();
        Reader reader = Reader.newReader(username, password, fullName, readerNumber,
                birthDate, phoneNumber, gdprConsent, marketingConsent, thirdPartyConsent);

        return readerRepository.save(reader);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<Reader> findReaderByReaderNumber(String readerNumber) {
        return readerRepository.findByReaderNumber(readerNumber);
    }

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("User is not logged in");
        }

        String subject = jwt.getClaimAsString("sub");
        String username = subject.contains(",") ? subject.split(",")[1] : subject;

        return findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    private String generateReaderNumber() {
        int year = Year.now().getValue();
        String yearPrefix = year + "/";

        Integer maxSeq = readerRepository.findMaxSequenceForYear(yearPrefix);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;

        return yearPrefix + nextSeq;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
