package pt.psoft.author.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.author.api.dto.AuthorView;
import pt.psoft.author.services.AuthorQueryService;
import pt.psoft.shared.exceptions.NotFoundException;

import java.util.List;

/**
 * REST Controller for Author Query operations (Read Side - CQRS)
 */
@Tag(name = "Authors - Queries", description = "Endpoints for querying Authors")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
public class AuthorQueryController {

    private final AuthorQueryService authorQueryService;

    @Operation(summary = "Get Author by number")
    @GetMapping("/{authorNumber}")
    public ResponseEntity<AuthorView> getAuthorByNumber(
            @PathVariable @Parameter(description = "Author number") Long authorNumber) {

        AuthorView author = authorQueryService.findByAuthorNumber(authorNumber);

        if (author == null) {
            throw new NotFoundException("Author with number " + authorNumber + " not found");
        }

        return ResponseEntity.ok()
                .eTag(String.valueOf(author.getVersion()))
                .body(author);
    }

    @Operation(summary = "Search Authors by name")
    @GetMapping
    public ResponseEntity<List<AuthorView>> searchAuthors(
            @RequestParam(required = false) @Parameter(description = "Name to search") String name) {

        List<AuthorView> authors = authorQueryService.searchByName(name);

        return ResponseEntity.ok(authors);
    }
}