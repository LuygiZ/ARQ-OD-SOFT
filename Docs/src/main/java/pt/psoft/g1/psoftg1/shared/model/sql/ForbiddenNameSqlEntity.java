package pt.psoft.g1.psoftg1.shared.model.sql;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="ForbiddenName")
@NoArgsConstructor
@Profile("sql-redis")
@Primary
public class ForbiddenNameSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Getter
    @Setter
    @Column(nullable = false)
    @Size(min = 1)
    private String forbiddenName;

    public ForbiddenNameSqlEntity(String forbiddenName)
    {
        setForbiddenName(forbiddenName);
    }
}
