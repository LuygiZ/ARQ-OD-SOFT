package pt.psoft.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.shared.dto.user.CreateUserRequest;
import pt.psoft.shared.dto.user.UserDTO;
import pt.psoft.shared.events.user.UserCreatedEvent;
import pt.psoft.user.messaging.UserEventPublisher;
import pt.psoft.user.model.User;
import pt.psoft.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO create(CreateUserRequest request) {
        // Basic validation/dup check logic here if needed
        
        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getFullName());
        userRepository.save(user);

        // Publish event to Outbox
        UserCreatedEvent event = new UserCreatedEvent(user.getUsername());
        userEventPublisher.publishUserCreated(event);

        return new UserDTO(user.getUsername(), user.getFullName());
    }
}
