package pt.psoft.author.services;

import pt.psoft.author.api.dto.AuthorView;

import java.util.List;

/**
 * Service for Author Query operations (Read Side - CQRS)
 */
public interface AuthorQueryService {

    /**
     * Find Author by author number
     */
    AuthorView findByAuthorNumber(Long authorNumber);

    /**
     * Search Authors by name (starts with)
     */
    List<AuthorView> searchByName(String name);

    /**
     * Get all Authors ordered by name
     */
    List<AuthorView> findAll();
}