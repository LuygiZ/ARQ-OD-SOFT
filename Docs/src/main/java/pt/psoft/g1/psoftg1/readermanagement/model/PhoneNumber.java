package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.Embeddable;

public class PhoneNumber
{
    private String phoneNumber;

    public PhoneNumber(String phoneNumber)
    {
        setPhoneNumber(phoneNumber);
    }

    // Setter
    private void setPhoneNumber(String phoneNumber)
    {
        if (phoneNumber == null || phoneNumber.isBlank())
        {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        if (!(phoneNumber.startsWith("9") || phoneNumber.startsWith("2")) || phoneNumber.length() != 9)
        {
            throw new IllegalArgumentException("Phone number is not valid: " + phoneNumber);
        }

        this.phoneNumber = phoneNumber;
    }

    // Getter
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    // Helper
    @Override
    public String toString()
    {
        return phoneNumber;
    }
}
