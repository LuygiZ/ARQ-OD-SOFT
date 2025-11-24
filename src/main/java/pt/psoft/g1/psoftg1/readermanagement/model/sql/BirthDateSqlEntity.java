package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.io.Serializable;
import java.time.LocalDate;

//@Entity
@Embeddable
@PropertySource({"classpath:config/library.properties"})
@Profile("sql")
@Primary
public class BirthDateSqlEntity implements Serializable
{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long pk;

    @Getter
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate birthDate;

    protected BirthDateSqlEntity() {}

    public BirthDateSqlEntity(LocalDate birthDate)
    {
        this.birthDate = birthDate;
    }
}
