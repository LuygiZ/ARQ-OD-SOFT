package pt.psoft.g1.psoftg1.usermanagement.model;

public class Reader extends User {
    protected Reader() {
        // for ORM only
    }

    public Reader(String username, String password) {
        super(username, password);
        this.addAuthority(new Role(Role.READER));
    }

    // Factory method para MapStruct usar ao carregar da BD
    public static Reader forMapper(String username, String password)
    {
        Reader reader = new Reader();
        reader.setUsername(username);
        reader.setPassword(password); // NÃO encripta
        return reader;
    }

    public static Reader newReader(final String username, final String password, final String name) {
        final var u = new Reader(username, password);
        u.setName(name);
        return u;
    }
}