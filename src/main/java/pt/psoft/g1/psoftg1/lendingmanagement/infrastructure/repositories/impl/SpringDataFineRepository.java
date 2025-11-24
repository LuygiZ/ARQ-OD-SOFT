package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.FineSqlEntity;

import java.util.Optional;

public interface SpringDataFineRepository extends CrudRepository<FineSqlEntity, Long>
{
    @Query("SELECT f " +
            "FROM FineSqlEntity f " +
            "JOIN LendingSqlEntity l ON f.lending.pk = l.pk " +
            "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<FineSqlEntity> findByLendingNumber(String lendingNumber);

    @Query("SELECT f FROM FineSqlEntity f")
    Iterable<FineSqlEntity> findAll();
}