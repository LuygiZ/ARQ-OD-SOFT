package pt.psoft.g1.psoftg1.usermanagement.model.sql;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;

@Profile("sql-redis")
@Primary
@Entity
@Table(name = "T_LIBRARIAN")
public class LibrarianSqlEntity extends UserSqlEntity
{
    protected LibrarianSqlEntity()
    {
        // for ORM only
    }
    public LibrarianSqlEntity(String username, String password, Role role)
    {
        super(username, password, role);
    }
}
