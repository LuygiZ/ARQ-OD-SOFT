package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql.sqlmapper.FineEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.FineSqlEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class FineRepositoryImpl implements FineRepository
{
    private final SpringDataFineRepository fineRepo;
    private final FineEntityMapper fineEntityMapper;

    @Override
    public Optional<Fine> findByLendingNumber(String lendingNumber)
    {
        Optional<FineSqlEntity> entityOpt = fineRepo.findByLendingNumber(lendingNumber);
        if (entityOpt.isPresent())
        {
            return Optional.of(fineEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Fine> findAll()
    {
        List<Fine> fines = new ArrayList<>();
        for (FineSqlEntity f: fineRepo.findAll())
        {
            fines.add(fineEntityMapper.toModel(f));
        }

        return fines;
    }

    @Override
    public Fine save(Fine fine)
    {
        return fineEntityMapper.toModel(fineRepo.save(fineEntityMapper.toEntity(fine)));
    }

}