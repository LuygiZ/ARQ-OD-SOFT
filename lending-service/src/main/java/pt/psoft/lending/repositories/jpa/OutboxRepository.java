package pt.psoft.lending.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.util.List;

/**
 * Repository for Outbox Events
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {

    /**
     * Find pending events ordered by creation time
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    /**
     * Find failed events for retry
     */
    List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxStatus status, int maxRetries);
}
