package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.mappers.UserEntityMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.LibrarianSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.ReaderSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL implementation of UserRepository using UserSqlEntity.
 */
@Configuration
@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository
{
    private final SpringDataUserRepository userRepo;
    private final UserEntityMapper userEntityMapper;
    private final EntityManager em;

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities)
    {
        List<S> savedEntities = new ArrayList<>();

        List<UserSqlEntity> userEntitiesToSave = new ArrayList<>();

        for (S entity : entities) {
            userEntitiesToSave.add(userEntityMapper.toEntity(entity));
        }

        for (UserSqlEntity userEntity : userRepo.saveAll(userEntitiesToSave)) {
            savedEntities.add((S) userEntityMapper.toModel(userEntity));
        }


        return savedEntities;
    }

    @Override
    @CacheEvict(value = "users", key = "#entity.username", condition = "#entity.username != null")
    public <S extends User> S save(S entity) {
        if (entity instanceof Reader) {
            ReaderSqlEntity readerEntity = userEntityMapper.toEntity((Reader) entity);
            ReaderSqlEntity savedEntity = userRepo.save(readerEntity);
            return (S) userEntityMapper.toModel(savedEntity);
        } else if (entity instanceof Librarian) {
            LibrarianSqlEntity librarianEntity = userEntityMapper.toEntity((Librarian) entity);
            LibrarianSqlEntity savedEntity = userRepo.save(librarianEntity);
            return (S) userEntityMapper.toModel(savedEntity);
        } else if (entity instanceof User) {
            UserSqlEntity userEntity = userEntityMapper.toEntity(entity);
            UserSqlEntity savedEntity = userRepo.save(userEntity);
            return (S) userEntityMapper.toModel(savedEntity);
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getName());
    }

    @Override
    public Optional<User> findById(Long objectId)
    {
        Optional<UserSqlEntity> entityOpt = userRepo.findById(objectId);
        if (entityOpt.isPresent())
        {
            return Optional.of(userEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(value = "users", key = "#username", unless = "#result == null || !#result.isPresent()")
    public Optional<User> findByUsername(String username) {
        Optional<UserSqlEntity> entityOpt = userRepo.findByUsername(username);
        return entityOpt.map(userEntityMapper::toModel);
    }

    @Override
    public List<User> searchUsers(Page page, SearchUsersQuery query)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<UserSqlEntity> cq = cb.createQuery(UserSqlEntity.class);
        final Root<UserSqlEntity> root = cq.from(UserSqlEntity.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();
        if (StringUtils.hasText(query.getUsername())) {
            where.add(cb.equal(root.get("username"), query.getUsername()));
        }
        if (StringUtils.hasText(query.getFullName())) {
            where.add(cb.like(root.get("fullName"), "%" + query.getFullName() + "%"));
        }

        // search using OR
        if (!where.isEmpty()) {
            cq.where(cb.or(where.toArray(new Predicate[0])));
        }

        cq.orderBy(cb.desc(root.get("createdAt")));

        final TypedQuery<UserSqlEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<User> users = new ArrayList<>();

        for (UserSqlEntity userEntity : q.getResultList()) {
            users.add(userEntityMapper.toModel(userEntity));
        }

        return users;
    }

    @Override
    public List<User> findByNameName(String name)
    {
        List<User> users = new ArrayList<>();
        for (UserSqlEntity r: userRepo.findByNameName(name))
        {
            users.add(userEntityMapper.toModel(r));
        }

        return users;
    }

    @Override
    public List<User> findByNameNameContains(String name)
    {
        List<User> users = new ArrayList<>();
        for (UserSqlEntity r: userRepo.findByNameNameContains(name))
        {
            users.add(userEntityMapper.toModel(r));
        }

        return users;
    }
    @Override
    public void delete(User user)
    {

    }

    // Novo method para retornar a entidade SQL diretamente
    public Optional<ReaderSqlEntity> findReaderEntityByUsername(String username)
    {
        Optional<UserSqlEntity> entityOpt = userRepo.findByUsername(username);
        if (entityOpt.isPresent() && entityOpt.get() instanceof ReaderSqlEntity)
        {
            return Optional.of((ReaderSqlEntity) entityOpt.get());
        }
        else
        {
            return Optional.empty();
        }
    }
}