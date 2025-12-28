package pt.psoft.author;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class for Author Service
 */
@SpringBootApplication(scanBasePackages = {"pt.psoft.author", "pt.psoft.shared"})
@EntityScan(basePackages = {"pt.psoft.author", "pt.psoft.shared"})
@EnableScheduling
public class AuthorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorServiceApplication.class, args);
    }
}