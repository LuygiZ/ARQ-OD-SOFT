package pt.psoft.g1.psoftg1.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * Embedded Redis server for development and testing.
 * Only active when using sql-redis or mongodb-redis profiles.
 *
 * In production, this should be disabled and an external Redis server should be used.
 * To disable: set persistence.use-embedded-redis=false in application.properties
 */
@Configuration
@Profile({"sql-redis", "mongodb-redis"})
@ConditionalOnProperty(name = "persistence.use-embedded-redis", havingValue = "true", matchIfMissing = true)
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
            System.out.println("Embedded Redis started on port 6379");
        } catch (Exception e) {
            System.err.println("Failed to start embedded Redis: " + e.getMessage());
            System.err.println("Make sure port 6379 is available or configure external Redis server");
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
            System.out.println("Embedded Redis stopped");
        }
    }
}
