package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingSqlEntity;

import java.util.List;
import java.util.Optional;

public interface SpringDataLendingRepository extends CrudRepository<LendingSqlEntity, Long>
{

    @Query("SELECT l " +
            "FROM LendingSqlEntity l " +
            "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<LendingSqlEntity> findByLendingNumber(String lendingNumber);

    //http://www.h2database.com/html/commands.html

    @Query("SELECT l " +
            "FROM LendingSqlEntity l " +
            "JOIN BookSqlEntity b ON l.book.pk = b.pk " +
            "JOIN ReaderDetailsSqlEntity r ON l.readerDetails.pk = r.pk " +
            "WHERE b.isbn.isbn = :isbn " +
            "AND r.readerNumber.readerNumber = :readerNumber ")
    List<LendingSqlEntity> listByReaderNumberAndIsbn(String readerNumber, String isbn);

    @Query("SELECT COUNT (l) " +
            "FROM LendingSqlEntity l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Query("SELECT l " +
            "FROM LendingSqlEntity l " +
            "JOIN ReaderDetailsSqlEntity r ON l.readerDetails.pk = r.pk " +
            "WHERE r.readerNumber.readerNumber = :readerNumber " +
            "AND l.returnedDate IS NULL")
    List<LendingSqlEntity> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);

    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM Lending l"
            , nativeQuery = true)
    Double getAverageDuration();

    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM Lending l " +
                    "JOIN BOOK b ON l.BOOK_PK = b.PK " +
                    "WHERE b.ISBN = :isbn"
            , nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}