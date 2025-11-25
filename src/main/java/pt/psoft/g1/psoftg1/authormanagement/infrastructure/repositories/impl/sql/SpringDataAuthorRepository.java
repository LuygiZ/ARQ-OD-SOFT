package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAuthorRepository extends CrudRepository<AuthorSqlEntity, Long> {

    @Query("SELECT a FROM AuthorSqlEntity a WHERE a.authorNumber = :authorNumber")
    Optional<AuthorSqlEntity> findByAuthorNumber(Long authorNumber);

    @Query("SELECT new pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView(a.name.name, COUNT(l.pk)) " +
            "FROM BookSqlEntity b " +
            "JOIN b.authors a " +
            "JOIN LendingSqlEntity l ON l.book.pk = b.pk " +
            "GROUP BY a.name " +
            "ORDER BY COUNT(l) DESC")
    Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable);

    @Query("SELECT DISTINCT coAuthor FROM BookSqlEntity b " +
            "JOIN b.authors coAuthor " +
            "WHERE b IN (SELECT b FROM BookSqlEntity b JOIN b.authors a WHERE a.authorNumber = :authorNumber) " +
            "AND coAuthor.authorNumber <> :authorNumber")
    List<AuthorSqlEntity> findCoAuthorsByAuthorNumber(Long authorNumber);

    @Query("SELECT a FROM AuthorSqlEntity a WHERE a.name.name = :name")
    List<AuthorSqlEntity> searchByNameName(String name);

    @Query("SELECT a FROM AuthorSqlEntity a WHERE a.name.name LIKE :name%")
    List<AuthorSqlEntity> searchByNameNameStartsWith(String name);

    @Query("SELECT a FROM AuthorSqlEntity a")
    Iterable<AuthorSqlEntity> findAll();
}

