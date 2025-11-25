package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.sql;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserSqlEntity;

import java.util.List;
import java.util.Optional;

@CacheConfig(cacheNames = "users")
public interface SpringDataUserRepository extends CrudRepository<UserSqlEntity, Long> {

    @CacheEvict(allEntries = true)
    <S extends UserSqlEntity> List<S> saveAll(Iterable<S> entities);

    @Caching(evict = { @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(key = "#p0.username", condition = "#p0.username != null") })
    <S extends UserSqlEntity> S save(S entity);

    /**
     * findById searches a specific user and returns an optional
     */

    @Cacheable
    Optional<UserSqlEntity> findById(Long objectId);

    /**
     * getById explicitly loads a user or throws an exception if the user does not
     * exist or the account is not enabled
     *
     * @param id
     * @return
     */
    @Cacheable
    default UserSqlEntity getById(final Long id) {
        final Optional<UserSqlEntity> maybeUser = findById(id);
        // throws 404 Not Found if the user does not exist or is not enabled
        return maybeUser.filter(UserSqlEntity::isEnabled).orElseThrow(() -> new NotFoundException(UserSqlEntity.class, id));
    }

    @Cacheable
    @Query("SELECT u FROM UserSqlEntity u WHERE u.username = ?1")
    Optional<UserSqlEntity> findByUsername(String username);

    @Cacheable
    @Query("SELECT u FROM UserSqlEntity u WHERE u.name.name = ?1")
    List<UserSqlEntity> findByNameName(String name);

    @Cacheable
    @Query("SELECT u FROM UserSqlEntity u WHERE LOWER(u.name.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<UserSqlEntity> findByNameNameContains(@Param("namePart") String namePart);
}