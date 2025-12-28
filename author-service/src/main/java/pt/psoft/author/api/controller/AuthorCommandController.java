package pt.psoft.author.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.author.api.dto.AuthorView;
import pt.psoft.author.api.CreateAuthorRequest;
import pt.psoft.author.api.UpdateAuthorRequest;
import pt.psoft.author.model.command.AuthorEntity;
import pt.psoft.author.services.AuthorCommandService;

/**
 * REST Controller for Author Command operations (Write Side - CQRS)
 */
@Tag(name = "Authors - Commands", description = "Endpoints for creating, updating, and deleting Authors")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
public class AuthorCommandController {

    private final AuthorCommandService authorCommandService;

    @Operation(summary = "Create a new Author")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthorView> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        AuthorEntity author = authorCommandService.createAuthor(request);

        var newAuthorUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{authorNumber}")
                .buildAndExpand(author.getAuthorNumber())
                .toUri();

        return ResponseEntity.created(newAuthorUri)
                .eTag(String.valueOf(author.getVersion()))
                .body(mapToAuthorView(author));
    }

    @Operation(summary = "Update an existing Author")
    @PatchMapping("/{authorNumber}")
    public ResponseEntity<AuthorView> updateAuthor(
            @PathVariable @Parameter(description = "Author number") Long authorNumber,
            @RequestHeader("If-Match") @Parameter(description = "Expected version") Long expectedVersion,
            @Valid @RequestBody UpdateAuthorRequest request) {

        AuthorEntity author = authorCommandService.updateAuthor(authorNumber, request, expectedVersion);

        return ResponseEntity.ok()
                .eTag(String.valueOf(author.getVersion()))
                .body(mapToAuthorView(author));
    }

    @Operation(summary = "Delete an Author")
    @DeleteMapping("/{authorNumber}")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable @Parameter(description = "Author number") Long authorNumber,
            @RequestHeader("If-Match") @Parameter(description = "Expected version") Long expectedVersion) {

        authorCommandService.deleteAuthor(authorNumber, expectedVersion);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove Author's photo")
    @DeleteMapping("/{authorNumber}/photo")
    public ResponseEntity<AuthorView> removeAuthorPhoto(
            @PathVariable @Parameter(description = "Author number") Long authorNumber,
            @RequestHeader("If-Match") @Parameter(description = "Expected version") Long expectedVersion) {

        AuthorEntity author = authorCommandService.removeAuthorPhoto(authorNumber, expectedVersion);

        return ResponseEntity.ok()
                .eTag(String.valueOf(author.getVersion()))
                .body(mapToAuthorView(author));
    }

    // Helper method to map AuthorEntity to AuthorView
    private AuthorView mapToAuthorView(AuthorEntity author) {
        return AuthorView.builder()
                .authorNumber(author.getAuthorNumber())
                .name(author.getNameValue())
                .bio(author.getBioValue())
                .photoURI(author.getPhotoURI())
                .version(author.getVersion())
                .build();
    }
}