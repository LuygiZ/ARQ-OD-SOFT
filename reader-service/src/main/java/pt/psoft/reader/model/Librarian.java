package pt.psoft.reader.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Librarian entity - extends User with librarian role
 */
@Entity
@Table(name = "librarians")
public class Librarian extends User {

    protected Librarian() {
        // For JPA
    }

    public Librarian(String username, String password) {
        super(username, password);
        this.addRole(Role.LIBRARIAN);
    }

    public static Librarian newLibrarian(String username, String password, String fullName) {
        Librarian librarian = new Librarian(username, password);
        librarian.setFullName(fullName);
        return librarian;
    }
}
