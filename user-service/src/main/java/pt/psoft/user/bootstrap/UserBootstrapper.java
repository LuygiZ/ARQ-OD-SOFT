package pt.psoft.user.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.user.model.Role;
import pt.psoft.user.model.User;
import pt.psoft.user.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@Profile("dev")
@Order(2)
public class UserBootstrapper implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(final String... args) {
        createLibrarian();
        createReaders();
    }

    private void createLibrarian() {
        if (userRepository.findByUsername("maria@gmail.com").isEmpty()) {
            User maria = new User("maria@gmail.com", passwordEncoder.encode("Mariaroberta!123"), "Maria Roberta");
            maria.addRole(Role.LIBRARIAN);
            userRepository.save(maria);
        }
    }

    private void createReaders() {
        //Reader1 - Manuel
        if (userRepository.findByUsername("manuel@gmail.com").isEmpty()) {
            User manuel = new User("manuel@gmail.com", passwordEncoder.encode("Manuelino123!"), "Manuel Sarapinto das Coives");
            manuel.addRole(Role.READER);
            userRepository.save(manuel);
        }

        //Reader2 - João
        if (userRepository.findByUsername("joao@gmail.com").isEmpty()) {
            User joao = new User("joao@gmail.com", passwordEncoder.encode("Joaoratao!123"), "João Ratao");
            joao.addRole(Role.READER);
            userRepository.save(joao);
        }

        //Reader3 - Pedro
        if (userRepository.findByUsername("pedro@gmail.com").isEmpty()) {
            User pedro = new User("pedro@gmail.com", passwordEncoder.encode("Pedrodascenas!123"), "Pedro Das Cenas");
            pedro.addRole(Role.READER);
            userRepository.save(pedro);
        }
        
        // Alice (Admin for testing)
        if (userRepository.findByUsername("admin1@mail.com").isEmpty()) {
            User admin = new User("admin1@mail.com", passwordEncoder.encode("Password1!"), "Admin User");
            admin.addRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
