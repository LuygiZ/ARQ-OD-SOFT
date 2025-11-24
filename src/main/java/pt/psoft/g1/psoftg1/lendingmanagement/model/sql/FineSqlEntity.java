package pt.psoft.g1.psoftg1.lendingmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Entity
@Table(name = "Fine")
@Profile("sql")
@Primary
public class FineSqlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @PositiveOrZero
    @Column(updatable = false)
    @Getter
    private int fineValuePerDayInCents;

    @PositiveOrZero
    @Getter
    private int centsValue;

    @Setter
    @Getter
    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "lending_pk", nullable = false, unique = true)
    private LendingSqlEntity lending;

    protected FineSqlEntity() { }

    public FineSqlEntity(int fineValuePerDayInCents, int centsValue, LendingSqlEntity lending)
    {
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.centsValue = centsValue;
        this.lending = lending;
    }
}
