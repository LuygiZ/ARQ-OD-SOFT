package pt.psoft.reader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"pt.psoft.reader", "pt.psoft.shared"})
@EnableJpaRepositories(basePackages = {"pt.psoft.reader.repositories"})
@ComponentScan(basePackages = {"pt.psoft.reader", "pt.psoft.shared"})
public class ReaderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReaderServiceApplication.class, args);
    }
}
