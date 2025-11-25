package pt.psoft.g1.psoftg1.usermanagement.model.mongodb;

import jakarta.persistence.Entity;

/*
@Entity
public class ReaderMongoEntity extends UserMongoEntity {
    protected ReaderMongoEntity() {
        // for ORM only
    }

    public ReaderMongoEntity(String username, String password) {
        super(username, password);
        this.addAuthority(new Role(Role.READER));
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


    public static ReaderMongoEntity newReader(final String username, final String password, final String name) {
        final var u = new ReaderMongoEntity(username, password);
        u.setName(name);
        return u;
    }
}
 */