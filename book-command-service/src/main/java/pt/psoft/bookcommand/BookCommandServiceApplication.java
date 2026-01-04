package pt.psoft.bookcommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"pt.psoft.bookcommand.model", "pt.psoft.shared.messaging"})
public class BookCommandServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCommandServiceApplication.class, args);
    }
}
