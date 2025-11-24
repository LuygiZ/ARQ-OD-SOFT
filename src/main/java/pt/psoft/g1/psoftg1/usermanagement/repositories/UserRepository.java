package pt.psoft.g1.psoftg1.usermanagement.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

/**
 * Repository interface for User management operations.
 */
public interface UserRepository {

    <S extends User> List<S> saveAll(Iterable<S> entities);

    <S extends User> S save(S entity);

    Optional<User> findById(Long objectId);

    default User getById(final Long id) {
        final Optional<User> maybeUser = findById(id);
        // throws 404 Not Found if the user does not exist or is not enabled
        return maybeUser.filter(User::isEnabled).orElseThrow(() -> new NotFoundException(User.class, id));
    }

    Optional<User> findByUsername(String username);

    List<User> searchUsers(Page page, SearchUsersQuery query);

    List<User> findByNameName(String name);
    List<User> findByNameNameContains(String name);
    void delete(User user);
}