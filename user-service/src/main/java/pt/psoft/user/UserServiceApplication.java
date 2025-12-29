package pt.psoft.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"pt.psoft.user", "pt.psoft.shared"})
@EnableJpaRepositories(basePackages = {"pt.psoft.user.repositories"})
@ComponentScan(basePackages = {"pt.psoft.user", "pt.psoft.shared"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
