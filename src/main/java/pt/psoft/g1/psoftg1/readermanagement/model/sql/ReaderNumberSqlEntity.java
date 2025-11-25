package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.Serializable;

//@Entity
@Embeddable
@Profile("sql-redis")
@Primary
public class ReaderNumberSqlEntity implements Serializable {

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long pk;

    @Column(name = "READER_NUMBER")
    private String readerNumber;

    protected ReaderNumberSqlEntity() {}

    public ReaderNumberSqlEntity(String readerNumber)
    {
        this.readerNumber = readerNumber;
    }

    public String getReaderNumber()
    {
        return readerNumber;
    }

    public String toString()
    {
        return readerNumber;
    }
}