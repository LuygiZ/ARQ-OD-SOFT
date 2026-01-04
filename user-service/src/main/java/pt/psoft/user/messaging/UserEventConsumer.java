package pt.psoft.user.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.shared.events.reader.ReaderCreatedEvent;
import pt.psoft.shared.events.user.UserCreatedEvent;
import pt.psoft.user.model.User;
import pt.psoft.user.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pt.psoft.user.config.RabbitmqConfig; // Assuming we will create this next

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @RabbitListener(queues = RabbitmqConfig.READER_CREATED_QUEUE)
    @Transactional
    public void handleReaderCreated(ReaderCreatedEvent event) {
        log.info("Received ReaderCreatedEvent for username: {}", event.getUsername());

        try {
            // 1. Check if user already exists
            if (userRepository.findByUsername(event.getUsername()).isPresent()) {
                log.warn("User already exists: {}", event.getUsername());
                publishResult(new UserCreatedEvent(event.getUsername(), "Username already exists"));
                return;
            }

            // 2. Create User
            User newUser = new User(event.getUsername(), passwordEncoder.encode(event.getPassword()), event.getFullName());
            newUser.addRole("READER");
            userRepository.save(newUser);
            log.info("User created successfully: {}", event.getUsername());

            // 3. Publish Success Event
            publishResult(new UserCreatedEvent(event.getUsername()));

        } catch (Exception e) {
            log.error("Error creating user for reader: {}", event.getUsername(), e);
            publishResult(new UserCreatedEvent(event.getUsername(), "Internal Server Error: " + e.getMessage()));
        }
    }

    private void publishResult(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME, "user.created", event);
        log.info("Published UserCreatedEvent (Success={}) for {}", event.isSuccess(), event.getUsername());
    }
}
