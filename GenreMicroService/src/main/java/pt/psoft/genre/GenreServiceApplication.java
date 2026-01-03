package pt.psoft.genre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling  // Para Outbox Publisher
@EntityScan(basePackages = {
        "pt.psoft.genre.model",           // Entities do genre-service
        "pt.psoft.shared.messaging"       // OutboxEvent do shared-kernel
})
public class GenreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenreServiceApplication.class, args);
    }
}