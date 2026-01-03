package pt.psoft.shared.shared.infrastructure.repositories.impl.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.mappers.ForbiddenNameEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.sql.ForbiddenNameSqlEntity;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class ForbiddenNameRepositoryImpl implements ForbiddenNameRepository
{
    private final SpringDataForbiddenNameRepository forbiddenNameRepository;
    private final ForbiddenNameEntityMapper forbiddenNameEntityMapper;

    @Override
    public Iterable<ForbiddenName> findAll()
    {
        List<ForbiddenName> fn = new ArrayList<>();
        for (ForbiddenNameSqlEntity f: forbiddenNameRepository.findAll())
        {
            fn.add(forbiddenNameEntityMapper.toModel(f));
        }

        return fn;
    }

    @Override
    public List<ForbiddenName> findByForbiddenNameIsContained(String pat)
    {
        List<ForbiddenName> fn = new ArrayList<>();
        for (ForbiddenNameSqlEntity f: forbiddenNameRepository.findByForbiddenNameIsContained(pat))
        {
            fn.add(forbiddenNameEntityMapper.toModel(f));
        }

        return fn;
    }

    @Override
    public ForbiddenName save(ForbiddenName forbiddenName)
    {
        return forbiddenNameEntityMapper.toModel(forbiddenNameRepository.save(forbiddenNameEntityMapper.toEntity(forbiddenName)));
    }

    @Override
    public Optional<ForbiddenName> findByForbiddenName(String forbiddenName)
    {
        Optional<ForbiddenNameSqlEntity> entityOpt = forbiddenNameRepository.findByForbiddenName(forbiddenName);
        if (entityOpt.isPresent())
        {
            return Optional.of(forbiddenNameEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public int deleteForbiddenName(String forbiddenName)
    {
        return forbiddenNameRepository.deleteForbiddenName(forbiddenName);
    }

}
