package pt.psoft.reader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reader/Auth Service Application
 * Handles user authentication and reader management
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = {"pt.psoft.reader", "pt.psoft.shared"})
public class ReaderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReaderServiceApplication.class, args);
    }
}
