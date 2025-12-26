package pt.psoft.book.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.shared.messaging.OutboxEvent;
import pt.psoft.shared.messaging.OutboxStatus;

import java.util.List;

/**
 * Repository for Outbox Pattern
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}