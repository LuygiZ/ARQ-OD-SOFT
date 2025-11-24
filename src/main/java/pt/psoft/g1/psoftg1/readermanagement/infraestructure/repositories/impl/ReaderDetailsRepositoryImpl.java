package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.sqlmapper.ReaderDetailsEntityMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.UserRepositoryImpl;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.ReaderSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserSqlEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql")
@Primary
@Repository
@RequiredArgsConstructor
public class ReaderDetailsRepositoryImpl implements ReaderRepository
{
    private final SpringDataReaderRepositoryImpl readerRepo;
    private final UserRepositoryImpl userRepo;
    private final ReaderDetailsEntityMapper readerEntityMapper;
    private final EntityManager entityManager;

    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber)
    {
        Optional<ReaderDetailsSqlEntity> entityOpt = readerRepo.findByReaderNumber(readerNumber);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber)
    {
        List<ReaderDetails> readers = new ArrayList<>();
        for (ReaderDetailsSqlEntity r: readerRepo.findByPhoneNumber(phoneNumber))
        {
            readers.add(readerEntityMapper.toModel(r));
        }

        return readers;
    }

    @Override
    public Optional<ReaderDetails> findByUsername(String username)
    {
        Optional<ReaderDetailsSqlEntity> entityOpt = readerRepo.findByUsername(username);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ReaderDetails> findByUserId(Long userId)
    {
        Optional<ReaderDetailsSqlEntity> entityOpt = readerRepo.findByUserId(userId);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public int getCountFromCurrentYear()
    {
        return readerRepo.getCountFromCurrentYear();
    }

    @Override
    public ReaderDetails save(ReaderDetails readerDetails)
    {
        ReaderDetailsSqlEntity readerDetailsEntity = readerEntityMapper.toEntity(readerDetails);

        // Buscar a entidade SQL diretamente
        ReaderSqlEntity userEntity = userRepo.findReaderEntityByUsername(
                readerDetails.getReader().getUsername()
        ).orElseThrow(() -> new RuntimeException("Reader not found"));

        readerDetailsEntity.setReader(userEntity);
        return readerEntityMapper.toModel(readerRepo.save(readerDetailsEntity));
    }

    @Override
    public Iterable<ReaderDetails> findAll()
    {
        List<ReaderDetails> readerDetails = new ArrayList<>();
        for (ReaderDetailsSqlEntity r: readerRepo.findAll())
        {
            readerDetails.add(readerEntityMapper.toModel(r));
        }

        return readerDetails;
    }

    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable)
    {
        return readerRepo.findTopReaders(pageable).map(readerEntityMapper::toModel);
    }

    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate)
    {
        return readerRepo.findTopByGenre(pageable, genre, startDate, endDate);
    }

    @Override
    public void delete(ReaderDetails readerDetails)
    {
        readerRepo.delete(readerEntityMapper.toEntity(readerDetails));
    }

    @Override
    public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query)
    {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ReaderDetailsSqlEntity> cq = cb.createQuery(ReaderDetailsSqlEntity.class);
        final Root<ReaderDetailsSqlEntity> readerDetailsRoot = cq.from(ReaderDetailsSqlEntity.class);
        Join<ReaderDetailsSqlEntity, UserSqlEntity> userJoin = readerDetailsRoot.join("reader");

        cq.select(readerDetailsRoot);

        final List<Predicate> where = new ArrayList<>();
        if (StringUtils.hasText(query.getName()))
        {
            //'contains' type search
            where.add(cb.like(userJoin.get("name").get("name"), "%" + query.getName() + "%"));
            cq.orderBy(cb.asc(userJoin.get("name")));
        }
        if (StringUtils.hasText(query.getEmail()))
        {
            //'exatct' type search
            where.add(cb.equal(userJoin.get("username"), query.getEmail()));
            cq.orderBy(cb.asc(userJoin.get("username")));
        }
        if (StringUtils.hasText(query.getPhoneNumber()))
        {
            //'exatct' type search
            where.add(cb.equal(readerDetailsRoot.get("phoneNumber").get("phoneNumber"), query.getPhoneNumber()));
            cq.orderBy(cb.asc(readerDetailsRoot.get("phoneNumber").get("phoneNumber")));
        }

        // search using OR
        if (!where.isEmpty())
        {
            cq.where(cb.or(where.toArray(new Predicate[0])));
        }


        final TypedQuery<ReaderDetailsSqlEntity> q = entityManager.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsSqlEntity readerDetail : q.getResultList())
        {
            readerDetails.add(readerEntityMapper.toModel(readerDetail));
        }

        return readerDetails;
    }

    // Adiciona este método público
    public Optional<ReaderDetailsSqlEntity> findSqlEntityByReaderNumber(String readerNumber) {
        return readerRepo.findByReaderNumber(readerNumber);
    }
}