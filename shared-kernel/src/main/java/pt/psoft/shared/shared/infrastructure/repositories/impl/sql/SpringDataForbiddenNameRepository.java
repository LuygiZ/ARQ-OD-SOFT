package pt.psoft.shared.shared.infrastructure.repositories.impl.sql;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.sql.ForbiddenNameSqlEntity;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataForbiddenNameRepository extends CrudRepository<ForbiddenNameSqlEntity, Long> {
    // Remove: extends ForbiddenNameRepository

    @Query("SELECT fn FROM ForbiddenNameSqlEntity fn" +
            " WHERE :pat LIKE CONCAT('%', fn.forbiddenName, '%') ")
    List<ForbiddenNameSqlEntity> findByForbiddenNameIsContained(String pat);

    @Query("SELECT fn " +
            "FROM ForbiddenNameSqlEntity fn " +
            "WHERE fn.forbiddenName = :forbiddenName")
    Optional<ForbiddenNameSqlEntity> findByForbiddenName(String forbiddenName);

    @Modifying
    @Query("DELETE FROM ForbiddenNameSqlEntity fn WHERE fn.forbiddenName = :forbiddenName")
    int deleteForbiddenName(String forbiddenName);
}
