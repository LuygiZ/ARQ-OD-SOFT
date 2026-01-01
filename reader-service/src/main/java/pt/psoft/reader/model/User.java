package pt.psoft.reader.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity implementing Spring Security UserDetails
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Setter
    private boolean enabled = true;

    @Column(unique = true, nullable = false)
    @Email
    @NotNull
    @NotBlank
    @Setter
    private String username;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    private String password;

    @Setter
    @Column(nullable = false)
    private String fullName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private final Set<String> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
        // For JPA
    }

    public User(String username, String password) {
        this.username = username;
        encodePassword(password);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static User newUser(String username, String password, String fullName) {
        User user = new User(username, password);
        user.setFullName(fullName);
        return user;
    }

    public static User newUser(String username, String password, String fullName, String role) {
        User user = newUser(username, password, fullName);
        user.addRole(role);
        return user;
    }

    public void encodePassword(String rawPassword) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(rawPassword);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }

    @Override
    public Set<Role> getAuthorities() {
        Set<Role> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(role));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
