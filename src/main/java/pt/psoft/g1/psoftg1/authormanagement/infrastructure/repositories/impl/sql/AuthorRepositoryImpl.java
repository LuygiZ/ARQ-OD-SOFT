package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.mappers.AuthorEntityMapper;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.redis.AuthorRepositoryRedisImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorSqlEntity;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
@Repository
@RequiredArgsConstructor
public class AuthorRepositoryImpl implements AuthorRepository
{
    private final SpringDataAuthorRepository authoRepo;
    private final AuthorEntityMapper authorEntityMapper;
    private final AuthorRepositoryRedisImpl redisRepo;  // ADICIONAR

    @Override
    public Optional<Author> findByAuthorNumber(Long authorNumber) {
        // 1. TENTAR BUSCAR DO REDIS
        String redisKey = "authors:author:" + authorNumber;
        Optional<Author> cached = redisRepo.getAuthorFromRedis(redisKey);

        if (cached.isPresent()) {
            System.out.println("✅ CACHE HIT - Author " + authorNumber + " from Redis");
            return cached;
        }

        // 2. SE NÃO EXISTIR, BUSCAR DO DB
        System.out.println("❌ CACHE MISS - Author " + authorNumber + " from DB");
        Optional<AuthorSqlEntity> entityOpt = authoRepo.findByAuthorNumber(authorNumber);

        if (entityOpt.isPresent()) {
            Author author = authorEntityMapper.toModel(entityOpt.get());

            // 3. CACHEAR NO REDIS
            redisRepo.save(author);

            return Optional.of(author);
        }

        return Optional.empty();
    }


    @Override
    public List<Author> searchByNameNameStartsWith(String name)
    {
        List<Author> authors = new ArrayList<>();
        for (AuthorSqlEntity a: authoRepo.searchByNameNameStartsWith(name))
        {
            authors.add(authorEntityMapper.toModel(a));
        }

        return authors;
    }

    @Override
    public List<Author> searchByNameName(String name)
    {
        List<Author> authors = new ArrayList<>();
        for (AuthorSqlEntity a: authoRepo.searchByNameName(name))
        {
            authors.add(authorEntityMapper.toModel(a));
        }

        return authors;
    }

    @Override
    public Author save(Author author) {
        // 1. SALVAR NO DB
        Author saved = authorEntityMapper.toModel(
                authoRepo.save(authorEntityMapper.toEntity(author))
        );

        // 2. INVALIDAR/ATUALIZAR CACHE
        redisRepo.save(saved);

        return saved;
    }

    @Override
    public Iterable<Author> findAll()
    {
        List<Author> authors = new ArrayList<>();
        for (AuthorSqlEntity a: authoRepo.findAll())
        {
            authors.add(authorEntityMapper.toModel(a));
        }

        return authors;
    }

    @Override
    public Page<AuthorLendingView> findTopAuthorByLendings (Pageable pageableRules)
    {
        return authoRepo.findTopAuthorByLendings(pageableRules);
    }

    @Override
    public void delete(Author author) {
        // 1. DELETE NO DB
        authoRepo.delete(authorEntityMapper.toEntity(author));

        // 2. DELETE NO CACHE
        redisRepo.delete(author);
    }

    @Override
    public List<Author> findCoAuthorsByAuthorNumber(Long authorNumber)
    {
        List<Author> authors = new ArrayList<>();
        for (AuthorSqlEntity a: authoRepo.findCoAuthorsByAuthorNumber(authorNumber))
        {
            authors.add(authorEntityMapper.toModel(a));
        }

        return authors;
    }
}
