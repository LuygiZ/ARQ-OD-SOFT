package pt.psoft.reader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;

@Entity
@Table(name = "readers")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @Version
    @Getter
    private Long version;

    // We store the username to link back to the User Service (logical link)
    @Column(unique = true, nullable = false)
    @Getter @Setter
    private String username;

    @Getter @Setter
    private String fullName;

    @Getter @Setter
    private String phoneNumber;

    @Getter @Setter
    private LocalDate birthDate;

    @Getter @Setter
    private boolean gdprConsent;

    @Getter @Setter
    private boolean marketingConsent;

    // Photo URL or path
    @Getter @Setter
    private String photoUrl;

    // Simplified list of interests (Genres) as Strings for now
    @ElementCollection
    @Getter @Setter
    private Set<String> interestList = new HashSet<>();

    protected Reader() {}

    public Reader(String username, String fullName, LocalDate birthDate, String phoneNumber, boolean gdprConsent) {
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.gdprConsent = gdprConsent;
    }
}
