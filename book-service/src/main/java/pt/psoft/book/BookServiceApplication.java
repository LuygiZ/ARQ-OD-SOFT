package pt.psoft.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EntityScan(basePackages = {
        "pt.psoft.book.command.model",      // Command Model entities
        "pt.psoft.book.query.model",        // Query Model entities (Read Model)
        "pt.psoft.shared.messaging"         // OutboxEvent from shared-kernel
})
@EnableJpaRepositories(basePackages = "pt.psoft.book")
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }
}