package pt.psoft.reader.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.reader.model.User;

import java.util.Optional;

/**
 * Repository for User entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:name%")
    java.util.List<User> findByNameContaining(@Param("name") String name);
}
