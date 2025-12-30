package pt.psoft.saga.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.saga.model.SagaInstance;

import java.util.Optional;

/**
 * Repository for Saga instances in Redis
 */
@Repository
public interface SagaRepository extends CrudRepository<SagaInstance, String> {

    /**
     * Find saga by ID
     */
    Optional<SagaInstance> findBySagaId(String sagaId);
}