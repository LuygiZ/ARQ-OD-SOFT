package pt.psoft.author.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.author.model.query.AuthorReadModel;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB Repository for Author Read Model
 * This is the Query side repository using MongoDB for Polyglot Persistence
 */
@Repository
public interface AuthorQueryRepository extends MongoRepository<AuthorReadModel, String> {

    /**
     * Find Author by business key (authorNumber from PostgreSQL)
     */
    Optional<AuthorReadModel> findByAuthorNumber(Long authorNumber);

    /**
     * Search Authors by name (starts with, case insensitive)
     * MongoDB's regex query
     */
    List<AuthorReadModel> findByNameStartingWithIgnoreCase(String name);

    /**
     * Find all Authors ordered by name ascending
     */
    List<AuthorReadModel> findAllByOrderByNameAsc();

    /**
     * Delete by author number (business key)
     */
    void deleteByAuthorNumber(Long authorNumber);

    /**
     * Check if author exists by author number
     */
    boolean existsByAuthorNumber(Long authorNumber);
}