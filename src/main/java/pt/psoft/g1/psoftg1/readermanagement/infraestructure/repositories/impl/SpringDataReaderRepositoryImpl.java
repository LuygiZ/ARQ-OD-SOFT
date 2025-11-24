package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Profile("sql")
public interface SpringDataReaderRepositoryImpl extends CrudRepository<ReaderDetailsSqlEntity, Long>
{
    @Query("SELECT r " +
            "FROM ReaderDetailsSqlEntity r " +
            "WHERE r.readerNumber.readerNumber = :readerNumber")
    Optional<ReaderDetailsSqlEntity> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

    @Query("SELECT r " +
            "FROM ReaderDetailsSqlEntity r " +
            "WHERE r.phoneNumber.phoneNumber = :phoneNumber")
    List<ReaderDetailsSqlEntity> findByPhoneNumber(@Param("phoneNumber") @NotNull String phoneNumber);

    @Query("SELECT r " +
            "FROM ReaderDetailsSqlEntity r " +
            "WHERE r.reader.username = :username")
    Optional<ReaderDetailsSqlEntity> findByUsername(@Param("username") @NotNull String username);

    @Query("SELECT r " +
            "FROM ReaderDetailsSqlEntity r " +
            "JOIN UserSqlEntity u ON r.reader.id = u.id " +
            "WHERE u.id = :userId")
    Optional<ReaderDetailsSqlEntity> findByUserId(@Param("userId") @NotNull Long userId);

    @Query("SELECT COUNT (rd) " +
            "FROM ReaderDetailsSqlEntity rd " +
            "JOIN UserSqlEntity u ON rd.reader.id = u.id " +
            "WHERE YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Query("SELECT rd " +
            "FROM ReaderDetailsSqlEntity rd " +
            "JOIN LendingSqlEntity l ON l.readerDetails.pk = rd.pk " +
            "GROUP BY rd " +
            "ORDER BY COUNT(l) DESC")
    Page<ReaderDetailsSqlEntity> findTopReaders(Pageable pageable);

    @Query("SELECT NEW pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO(rd, count(l)) " +
            "FROM ReaderDetailsSqlEntity rd " +
            "JOIN LendingSqlEntity l ON l.readerDetails.pk = rd.pk " +
            "JOIN BookSqlEntity b ON b.pk = l.book.pk " +
            "JOIN GenreSqlEntity g ON g.pk = b.genre.pk " +
            "WHERE g.genre = :genre " +
            "AND l.startDate >= :startDate " +
            "AND l.startDate <= :endDate " +
            "GROUP BY rd.pk " +
            "ORDER BY COUNT(l.pk) DESC")
    Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate);

    @Query("SELECT r FROM ReaderDetailsSqlEntity r")
    Iterable<ReaderDetailsSqlEntity> findAll();
}