package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sql.BookRepositoryImpl;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql.sqlmapper.LendingEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingSqlEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.sql.ReaderDetailsRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsSqlEntity;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class LendingRepositoryImpl implements LendingRepository
{
    private final SpringDataLendingRepository lendingRepo;
    private final LendingEntityMapper lendingEntityMapper;
    private final EntityManager em;
    private final BookRepositoryImpl bookRepo;
    private final ReaderDetailsRepositoryImpl readerDetailsRepo;

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber)
    {
        Optional<LendingSqlEntity> entityOpt = lendingRepo.findByLendingNumber(lendingNumber);
        if (entityOpt.isPresent())
        {
            return Optional.of(lendingEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn)
    {
        List<Lending> lendings = new ArrayList<>();
        for (LendingSqlEntity l: lendingRepo.listByReaderNumberAndIsbn(readerNumber, isbn))
        {
            lendings.add(lendingEntityMapper.toModel(l));
        }

        return lendings;
    }

    @Override
    public int getCountFromCurrentYear()
    {
        return lendingRepo.getCountFromCurrentYear();
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber)
    {
        List<Lending> lendings = new ArrayList<>();
        for (LendingSqlEntity l: lendingRepo.listOutstandingByReaderNumber(readerNumber))
        {
            lendings.add(lendingEntityMapper.toModel(l));
        }

        return lendings;
    }

    @Override
    public Double getAverageDuration()
    {
        return lendingRepo.getAverageDuration();
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn)
    {
        return lendingRepo.getAvgLendingDurationByIsbn(isbn);
    }

    @Override
    public List<Lending> getOverdue(Page page)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingSqlEntity> cq = cb.createQuery(LendingSqlEntity.class);
        final Root<LendingSqlEntity> root = cq.from(LendingSqlEntity.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        // Select overdue lendings where returnedDate is null and limitDate is before the current date
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate"))); // Order by limitDate, oldest first

        final TypedQuery<LendingSqlEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<Lending> lendings = new ArrayList<>();

        for (LendingSqlEntity lendingEntity : q.getResultList()) {
            lendings.add(lendingEntityMapper.toModel(lendingEntity));
        }

        return lendings;
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingSqlEntity> cq = cb.createQuery(LendingSqlEntity.class);
        final Root<LendingSqlEntity> lendingRoot = cq.from(LendingSqlEntity.class);
        final Join<LendingSqlEntity, Book> bookJoin = lendingRoot.join("book");
        final Join<LendingSqlEntity, ReaderDetails> readerDetailsJoin = lendingRoot.join("readerDetails");
        cq.select(lendingRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(readerNumber))
        {
            where.add(cb.like(readerDetailsJoin.get("readerNumber").get("readerNumber"), readerNumber));
        }
        if (StringUtils.hasText(isbn))
        {
            where.add(cb.like(bookJoin.get("isbn").get("isbn"), isbn));
        }
        if (returned != null)
        {
            if(returned)
            {
                where.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            }
            else
            {
                where.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if(startDate!=null)
        {
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        }
        if(endDate!=null)
        {
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));
        }

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        final TypedQuery<LendingSqlEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<Lending> lendings = new ArrayList<>();

        for (LendingSqlEntity lendingEntity : q.getResultList())
        {
            lendings.add(lendingEntityMapper.toModel(lendingEntity));
        }

        return lendings;
    }
    
    @Override
    public Lending save(Lending lending) {
        LendingSqlEntity entity = lendingEntityMapper.toEntity(lending);

        // Buscar as entidades SQL usando os novos métodos helper
        BookSqlEntity bookEntity = bookRepo.findSqlEntityByIsbn(
                lending.getBorrowedBook().getIsbn().toString()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Book not found with ISBN: " + lending.getBorrowedBook().getIsbn().toString()));

        ReaderDetailsSqlEntity readerEntity = readerDetailsRepo.findSqlEntityByReaderNumber(
                lending.getBorrower().getReaderNumber()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Reader not found with number: " + lending.getBorrower().getReaderNumber()));

        // Setar as entidades managed
        entity.setBook(bookEntity);
        entity.setReaderDetails(readerEntity);

        // Salvar
        LendingSqlEntity savedEntity = lendingRepo.save(entity);
        return lendingEntityMapper.toModel(savedEntity);
    }

    @Override
    public void delete(Lending lending)
    {
        lendingRepo.delete(lendingEntityMapper.toEntity(lending));
    }

}
