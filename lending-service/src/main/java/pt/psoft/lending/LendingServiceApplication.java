package pt.psoft.lending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lending Service Application
 * Microservice for managing book lendings and returns
 * Student C - ARQSOFT 2025/2026
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = {"pt.psoft.lending", "pt.psoft.shared"})
public class LendingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LendingServiceApplication.class, args);
    }
}
