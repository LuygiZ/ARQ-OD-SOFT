package pt.psoft.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"pt.psoft.user", "pt.psoft.shared"})
@org.springframework.context.annotation.ComponentScan(basePackages = {"pt.psoft.user", "pt.psoft.shared"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
