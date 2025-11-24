package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Embeddable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.Serializable;

//@Entity
@Embeddable
@Profile("sql")
@Primary
public class PhoneNumberSqlEntity implements Serializable
{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long pk;

    private String phoneNumber;

    protected PhoneNumberSqlEntity() {}

    public PhoneNumberSqlEntity(String phoneNumber)
    {
        setPhoneNumber(phoneNumber);
    }

    // Setter
    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    // Getter
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    // Helper
    public String toString()
    {
        return phoneNumber;
    }
}