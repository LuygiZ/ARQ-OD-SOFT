package pt.psoft.bookquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EntityScan(basePackages = {"pt.psoft.bookquery.model"})
public class BookQueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookQueryServiceApplication.class, args);
    }
}
