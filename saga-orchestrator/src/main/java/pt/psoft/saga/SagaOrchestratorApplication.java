package pt.psoft.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Saga Orchestrator Service
 *
 * Coordinates distributed transactions across:
 * - Genre Service
 * - Author Service
 * - Book Service
 *
 * Uses Saga Orchestration Pattern with:
 * - Redis for state management
 * - OpenFeign for service communication
 * - Resilience4j for fault tolerance
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class SagaOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaOrchestratorApplication.class, args);
    }
}