package pt.psoft.reader.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.psoft.reader.api.dto.ReaderView;
import pt.psoft.reader.model.Reader;
import pt.psoft.reader.model.Role;
import pt.psoft.reader.repositories.ReaderRepository;
import pt.psoft.reader.services.UserService;
import pt.psoft.shared.exceptions.NotFoundException;

import java.util.stream.Collectors;

/**
 * REST Controller for Reader operations
 */
@Tag(name = "Readers", description = "Endpoints for reader management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readers")
public class ReaderController {

    private final ReaderRepository readerRepository;
    private final UserService userService;

    @Operation(summary = "Get reader by reader number")
    @GetMapping("/{year}/{sequence}")
    public ResponseEntity<ReaderView> getReader(
            @PathVariable @Parameter(description = "Year part of reader number") int year,
            @PathVariable @Parameter(description = "Sequence part of reader number") int sequence) {

        String readerNumber = year + "/" + sequence;
        Reader reader = readerRepository.findByReaderNumber(readerNumber)
                .orElseThrow(() -> new NotFoundException("Reader not found: " + readerNumber));

        return ResponseEntity.ok()
                .eTag(String.valueOf(reader.getVersion()))
                .body(mapToReaderView(reader));
    }

    @Operation(summary = "Get current authenticated reader's profile")
    @GetMapping("/me")
    public ResponseEntity<ReaderView> getMyProfile(Authentication authentication) {
        Reader reader = (Reader) userService.getAuthenticatedUser(authentication);
        return ResponseEntity.ok()
                .eTag(String.valueOf(reader.getVersion()))
                .body(mapToReaderView(reader));
    }

    @Operation(summary = "List all readers (Librarian only)")
    @GetMapping
    public ResponseEntity<Page<ReaderView>> listReaders(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Reader> readers = readerRepository.findAll(pageable);
        Page<ReaderView> views = readers.map(this::mapToReaderView);

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Search readers by name")
    @GetMapping("/search")
    public ResponseEntity<Page<ReaderView>> searchReaders(
            @RequestParam @Parameter(description = "Name to search") String name,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Reader> readers = readerRepository.findByNameContaining(name, pageable);
        Page<ReaderView> views = readers.map(this::mapToReaderView);

        return ResponseEntity.ok(views);
    }

    @Operation(summary = "Check if reader exists by reader number")
    @GetMapping("/{year}/{sequence}/exists")
    public ResponseEntity<Boolean> readerExists(
            @PathVariable int year,
            @PathVariable int sequence) {

        String readerNumber = year + "/" + sequence;
        boolean exists = readerRepository.existsByReaderNumber(readerNumber);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Get reader count")
    @GetMapping("/count")
    public ResponseEntity<Long> getReaderCount() {
        return ResponseEntity.ok(readerRepository.countReaders());
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
