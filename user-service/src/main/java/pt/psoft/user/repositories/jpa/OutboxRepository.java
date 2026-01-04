package pt.psoft.user.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.util.List;

/**
 * Repository for Outbox Events (Transactional Outbox Pattern)
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
