package pt.psoft.g1.psoftg1.lendingmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsSqlEntity;
import java.time.LocalDate;

@Profile("sql-redis")
@Primary
@Entity
@Data
@Table(name="Lending", uniqueConstraints = {
        @UniqueConstraint(columnNames={"LENDING_NUMBER"})})

public class LendingSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Embedded
    private LendingNumberSqlEntity lendingNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_fk")
    private BookSqlEntity book;

    @NotNull
    @Getter
    @ManyToOne(fetch=FetchType.EAGER, optional = false)
    private ReaderDetailsSqlEntity readerDetails;

    @NotNull
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate limitDate;

    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate returnedDate;

    @Version
    @Getter
    private long version;

    @Size(min = 0, max = 1024)
    @Column(length = 1024)
    @Getter
    private String commentary;

    @Getter
    private int fineValuePerDayInCents;

    @Transient
    private Integer daysUntilReturn;

    @Transient
    private Integer daysOverdue;

    protected LendingSqlEntity() {}

    public LendingSqlEntity(BookSqlEntity book, ReaderDetailsSqlEntity readerDetails, LendingNumberSqlEntity lendingNumber,
                            LocalDate startDate, LocalDate limitDate, LocalDate returnedDate, int fineValuePerDayInCents,
                            String commentary) {
        this.book = book;
        this.readerDetails = readerDetails;
        this.lendingNumber = lendingNumber;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.commentary = commentary;
    }
}

