package pt.psoft.g1.psoftg1.usermanagement.model.mongodb;

import jakarta.persistence.Entity;
import pt.psoft.g1.psoftg1.shared.model.Name;

/*
@Entity
public class LibrarianMongoEntity extends UserMongoEntity {
    protected LibrarianMongoEntity() {
        // for ORM only
    }

    public LibrarianMongoEntity(String username, String password) {
        super(username, password);
    }

    /**
     * factory method. since mapstruct does not handle protected/private setters
     * neither more than one public constructor, we use these factory methods for
     * helper creation scenarios
     *
     * @param username
     * @param password
     * @param name
     * @return


    public static LibrarianMongoEntity newLibrarian(final String username, final String password, final String name) {
        final var u = new LibrarianMongoEntity(username, password);
        u.setName(name);
        u.addAuthority(new Role(Role.LIBRARIAN));
        return u;
    }
}
        */