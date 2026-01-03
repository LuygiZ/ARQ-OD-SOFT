package pt.psoft.author;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class for Author Service
 *
 * Implements Polyglot Persistence:
 * - PostgreSQL: Command Model (Write Side) via JPA
 * - MongoDB: Read Model (Read Side) via Spring Data MongoDB
 * - Redis: Caching layer
 * - RabbitMQ: Event-driven synchronization
 */
@SpringBootApplication(scanBasePackages = {"pt.psoft.author", "pt.psoft.shared"})
@EntityScan(basePackages = {"pt.psoft.author.model.command.valueobjects", "pt.psoft.author.model.command", "pt.psoft.shared.messaging"})
@EnableJpaRepositories(basePackages = "pt.psoft.author.repositories.jpa")
@EnableMongoRepositories(basePackages = "pt.psoft.author.repositories.mongo")
@EnableScheduling
public class AuthorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorServiceApplication.class, args);
    }
}