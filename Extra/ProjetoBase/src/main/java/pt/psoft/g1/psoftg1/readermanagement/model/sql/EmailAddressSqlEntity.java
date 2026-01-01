package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.Serializable;

//@Entity
@Embeddable
@Profile("sql-redis")
@Primary
public class EmailAddressSqlEntity implements Serializable
{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long pk;

    @Email
    private String address;

    protected EmailAddressSqlEntity() {}

    public EmailAddressSqlEntity(String address)
    {
        this.address = address;
    }

    // Getter
    public String getAddress()
    {
        return address;
    }

    // Helper
    public String toString() {
        return address;
    }
}