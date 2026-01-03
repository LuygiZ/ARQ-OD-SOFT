package pt.psoft.book.genremanagement.infrastructure.repositories.impl.sql;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreSqlEntity;

import java.util.List;
import java.util.Optional;

public interface SpringDataGenreRepository extends CrudRepository<GenreSqlEntity, Integer> {

    @Query("SELECT g FROM GenreSqlEntity g")
    List<GenreSqlEntity> findAllGenres();

    @Query("SELECT g FROM GenreSqlEntity g WHERE g.genre = :genreName" )
    Optional<GenreSqlEntity> findByString(@Param("genreName")@NotNull String genre);

    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO(g.genre, COUNT(b))" +
            "FROM GenreSqlEntity g " +
            "JOIN BookSqlEntity b ON b.genre.pk = g.pk " +
            "GROUP BY g " +
            "ORDER BY COUNT(b) DESC")
    Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);
}