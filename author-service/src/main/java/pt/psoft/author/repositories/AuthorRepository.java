package pt.psoft.author.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.author.model.command.AuthorEntity;

import java.util.Optional;

/**
 * Repository for Author Command Model (Write Side)
 */
@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    @Query("SELECT a FROM AuthorEntity a WHERE a.authorNumber = :authorNumber")
    Optional<AuthorEntity> findByAuthorNumber(Long authorNumber);

    @Query("SELECT COUNT(a) > 0 FROM AuthorEntity a WHERE a.authorNumber = :authorNumber")
    boolean existsByAuthorNumber(Long authorNumber);
}