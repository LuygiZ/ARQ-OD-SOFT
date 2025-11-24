package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;

public class EmailAddress {

    private final String address;

    public EmailAddress(String address)
    {
        if (address == null || address.isBlank())
        {
            throw new IllegalArgumentException("This email address cannot be null or blank");
        }
        this.address = address;
    }

    // Getter
    public String getAddress()
    {
        return address;
    }

    // Helper
    public String toString()
    {
        return address;
    }
}
