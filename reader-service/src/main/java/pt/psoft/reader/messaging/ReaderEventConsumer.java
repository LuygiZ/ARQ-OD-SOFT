package pt.psoft.reader.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.shared.events.user.UserCreatedEvent;
import pt.psoft.reader.repositories.ReaderRepository;
import pt.psoft.reader.config.RabbitmqConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReaderEventConsumer {

    private final ReaderRepository readerRepository;

    @RabbitListener(queues = RabbitmqConfig.USER_CREATED_QUEUE)
    @Transactional
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for username: {}, success: {}", event.getUsername(), event.isSuccess());

        readerRepository.findByUsername(event.getUsername()).ifPresentOrElse(reader -> {
            if (event.isSuccess()) {
                log.info("SAGA SUCCESS: Reader and User created for {}", event.getUsername());
                // In a real system, we might update status from PENDING to ACTIVE here
            } else {
                log.warn("SAGA FAILURE: User creation failed for {}. Rolling back Reader...", event.getUsername());
                // Compensating Transaction: Delete the reader
                readerRepository.delete(reader);
                log.info("Rollback complete. Reader deleted.");
            }
        }, () -> {
            log.error("Received event for unknown reader: {}", event.getUsername());
        });
    }
}
