package pt.psoft.author.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration for Author Service Read Model
 *
 * Enables MongoDB repositories and auditing
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Spring Boot auto-configuration handles connection via application.yml
}