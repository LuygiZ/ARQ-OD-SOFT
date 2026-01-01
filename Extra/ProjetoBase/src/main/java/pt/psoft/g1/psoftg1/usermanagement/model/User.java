package pt.psoft.g1.psoftg1.usermanagement.model;

import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.psoft.g1.psoftg1.shared.model.Name;

public class User implements UserDetails
{
    private Long id;
    private Long version;
    private boolean enabled = true;
    private String username;
    private String password;
    private Name name;
    private final Set<Role> authorities = new HashSet<>();

    protected User() {}

    public User(final String username, final String password)
    {
        setUsername(username);
        encodePassword(password);
    }

    // Factory method para MapStruct usar ao carregar da BD
    public static User forMapper(String username, String password)
    {
        User user = new User();
        user.username = username;
        user.password = password; // NÃO encripta - já vem encriptada da BD
        return user;
    }

    public static User newUser(final String username, final String password, final String name)
    {
        final var u = new User(username, password);
        u.setName(name);
        return u;
    }

    public static User newUser(final String username, final String password, final String name, final String role)
    {
        final var u = new User(username, password);
        u.setName(name);
        u.addAuthority(new Role(role));
        return u;
    }

    public void encodePassword(final String rawPassword)
    {
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(rawPassword);
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public void addAuthority(final Role r)
    {
        authorities.add(r);
    }

    public void setName(String name)
    {
        this.name = new Name(name);
    }

    public void setEnabled(boolean value)
    {
        this.enabled = value;
    }

    // getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Name getName() { return name; }
    public boolean isEnabled() { return enabled; }
    @Override
    public Set<Role> getAuthorities() { return authorities; }
    @Override
    public boolean isAccountNonExpired() { return isEnabled(); }
    @Override
    public boolean isAccountNonLocked() { return isEnabled(); }
    @Override
    public boolean isCredentialsNonExpired() { return isEnabled(); }
    public Long getId() { return id; }
    public Long getVersion() { return version; }
}