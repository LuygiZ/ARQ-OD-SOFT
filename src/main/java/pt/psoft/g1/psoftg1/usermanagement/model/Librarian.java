package pt.psoft.g1.psoftg1.usermanagement.model;

public class Librarian extends User {
    protected Librarian() {
        // for ORM only
    }

    public Librarian(String username, String password) {
        super(username, password);
    }

    // Factory method para MapStruct usar ao carregar da BD
    public static Librarian forMapper(String username, String password)
    {
        Librarian librarian = new Librarian();
        librarian.setUsername(username);
        librarian.setPassword(password); // NÃO encripta
        return librarian;
    }

    public static Librarian newLibrarian(final String username, final String password, final String name) {
        final var u = new Librarian(username, password);
        u.setName(name);
        u.addAuthority(new Role(Role.LIBRARIAN));
        return u;
    }
}