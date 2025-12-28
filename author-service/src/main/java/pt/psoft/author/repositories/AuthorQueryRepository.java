package pt.psoft.author.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.author.model.query.AuthorReadModel;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Author Query Model (Read Side)
 */
@Repository
public interface AuthorQueryRepository extends JpaRepository<AuthorReadModel, Long> {

    @Query("SELECT a FROM AuthorReadModel a WHERE a.authorNumber = :authorNumber")
    Optional<AuthorReadModel> findByAuthorNumber(Long authorNumber);

    @Query("SELECT a FROM AuthorReadModel a WHERE LOWER(a.name) LIKE LOWER(CONCAT(:name, '%'))")
    List<AuthorReadModel> findByNameStartingWithIgnoreCase(String name);

    @Query("SELECT a FROM AuthorReadModel a ORDER BY a.name ASC")
    List<AuthorReadModel> findAllOrderByName();
}