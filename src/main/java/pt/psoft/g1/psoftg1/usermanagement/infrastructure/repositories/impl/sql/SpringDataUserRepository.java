package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.sql;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserSqlEntity;

import java.util.List;
import java.util.Optional;

// REMOVER: @CacheConfig(cacheNames = "users")
public interface SpringDataUserRepository extends CrudRepository<UserSqlEntity, Long> {

    // REMOVER: @CacheEvict(allEntries = true)
    <S extends UserSqlEntity> List<S> saveAll(Iterable<S> entities);

    // REMOVER: @Caching(evict = { @CacheEvict...})
    <S extends UserSqlEntity> S save(S entity);

    // REMOVER: @Cacheable
    Optional<UserSqlEntity> findById(Long objectId);

    // REMOVER: @Cacheable
    default UserSqlEntity getById(final Long id) {
        final Optional<UserSqlEntity> maybeUser = findById(id);
        return maybeUser.filter(UserSqlEntity::isEnabled)
                .orElseThrow(() -> new NotFoundException(UserSqlEntity.class, id));
    }

    // ADICIONAR: @EntityGraph para resolver lazy loading
    // REMOVER: @Cacheable
    @EntityGraph(attributePaths = {"authorities"})
    @Query("SELECT u FROM UserSqlEntity u WHERE u.username = ?1")
    Optional<UserSqlEntity> findByUsername(String username);

    // REMOVER: @Cacheable
    @Query("SELECT u FROM UserSqlEntity u WHERE u.name.name = ?1")
    List<UserSqlEntity> findByNameName(String name);

    // REMOVER: @Cacheable
    @Query("SELECT u FROM UserSqlEntity u WHERE LOWER(u.name.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<UserSqlEntity> findByNameNameContains(@Param("namePart") String namePart);
}