package pt.psoft.reader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Reader entity - extends User with reader-specific attributes
 */
@Entity
@Table(name = "readers")
@Getter
public class Reader extends User {

    @Column(unique = true, nullable = false)
    private String readerNumber;

    @Column(name = "birth_date")
    @Setter
    private LocalDate birthDate;

    @Column(name = "phone_number")
    @Setter
    private String phoneNumber;

    @Column(name = "gdpr_consent", nullable = false)
    private boolean gdprConsent;

    @Column(name = "marketing_consent")
    @Setter
    private boolean marketingConsent;

    @Column(name = "third_party_consent")
    @Setter
    private boolean thirdPartyConsent;

    protected Reader() {
        // For JPA
    }

    public Reader(String username, String password) {
        super(username, password);
        this.addRole(Role.READER);
    }

    public static Reader newReader(String username, String password, String fullName,
                                   String readerNumber, LocalDate birthDate, String phoneNumber,
                                   boolean gdprConsent, boolean marketingConsent, boolean thirdPartyConsent) {
        Reader reader = new Reader(username, password);
        reader.setFullName(fullName);
        reader.readerNumber = readerNumber;
        reader.birthDate = birthDate;
        reader.phoneNumber = phoneNumber;
        reader.gdprConsent = gdprConsent;
        reader.marketingConsent = marketingConsent;
        reader.thirdPartyConsent = thirdPartyConsent;
        return reader;
    }

    public static Reader newReader(String username, String password, String fullName, String readerNumber) {
        return newReader(username, password, fullName, readerNumber, null, null, true, false, false);
    }

    public void setReaderNumber(String readerNumber) {
        this.readerNumber = readerNumber;
    }
}
