package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sql;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoSqlEntity;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

public interface SpringDataPhotoRepository extends CrudRepository<PhotoSqlEntity, Long> {
    // Remove: extends PhotoRepository

    @Modifying
    @Transactional
    @Query("DELETE FROM PhotoSqlEntity p WHERE p.photoFile = :photoFile")
    void deleteByPhotoFile(String photoFile);
}
