package pt.psoft.g1.psoftg1.lendingmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import java.io.Serializable;

//@Entity
@Embeddable
@EqualsAndHashCode
@Profile("sql")
@Primary
public class LendingNumberSqlEntity implements Serializable
{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long pk;

    @Column(name = "LENDING_NUMBER", length = 32)
    @NotNull
    @NotBlank
    @Getter
    @Size(min = 6, max = 32)
    private String lendingNumber;

    protected LendingNumberSqlEntity() { }

    public LendingNumberSqlEntity(String lendingNumber)
    {
        setValue(lendingNumber);
    }

    // Getter
    public String getValue()
    {
        return lendingNumber;
    }

    // Setter
    public void setValue(String lendingNumber)
    {
        this.lendingNumber = lendingNumber;
    }

    // Helper
    public String toString()
    {
        return this.lendingNumber;
    }
}

