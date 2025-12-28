package pt.psoft.author.services;

import pt.psoft.author.api.CreateAuthorRequest;
import pt.psoft.author.api.UpdateAuthorRequest;
import pt.psoft.author.model.command.AuthorEntity;

/**
 * Service for Author Command operations (Write Side - CQRS)
 */
public interface AuthorCommandService {

    /**
     * Create a new Author
     */
    AuthorEntity createAuthor(CreateAuthorRequest request);

    /**
     * Update an existing Author
     */
    AuthorEntity updateAuthor(Long authorNumber, UpdateAuthorRequest request, Long expectedVersion);

    /**
     * Delete an Author
     */
    void deleteAuthor(Long authorNumber, Long expectedVersion);

    /**
     * Remove Author's photo
     */
    AuthorEntity removeAuthorPhoto(Long authorNumber, Long expectedVersion);
}