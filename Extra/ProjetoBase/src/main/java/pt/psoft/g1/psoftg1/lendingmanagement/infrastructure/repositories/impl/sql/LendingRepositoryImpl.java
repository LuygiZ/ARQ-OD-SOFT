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
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.mappers.LendingEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.redis.LendingRepositoryRedisImpl;
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
public class LendingRepositoryImpl implements LendingRepository {

    private final SpringDataLendingRepository lendingRepo;
    private final LendingEntityMapper lendingEntityMapper;
    private final EntityManager em;
    private final BookRepositoryImpl bookRepo;
    private final ReaderDetailsRepositoryImpl readerDetailsRepo;
    private final LendingRepositoryRedisImpl redisRepo;  // ADICIONAR

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        // 1. TENTAR BUSCAR DO REDIS
        String redisKey = "lendings:number:" + lendingNumber;
        Optional<Lending> cached = redisRepo.getLendingFromRedis(redisKey);

        if (cached.isPresent()) {
            System.out.println("✅ CACHE HIT - Lending " + lendingNumber + " from Redis");
            return cached;
        }

        // 2. SE NÃO EXISTIR, BUSCAR DO DB
        System.out.println("❌ CACHE MISS - Lending " + lendingNumber + " from DB");

        Optional<LendingSqlEntity> entityOpt = lendingRepo.findByLendingNumber(lendingNumber);

        if (entityOpt.isPresent()) {
            Lending lending = lendingEntityMapper.toModel(entityOpt.get());

            // 3. CACHEAR NO REDIS
            redisRepo.save(lending);

            return Optional.of(lending);
        }

        return Optional.empty();
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        // 1. TENTAR BUSCAR DO REDIS
        Optional<List<Lending>> cached = redisRepo.getOutstandingByReaderFromRedis(readerNumber);

        if (cached.isPresent()) {
            System.out.println("✅ CACHE HIT - Outstanding lendings for reader " + readerNumber + " from Redis");
            return cached.get();
        }

        // 2. SE NÃO EXISTIR, BUSCAR DO DB
        System.out.println("❌ CACHE MISS - Outstanding lendings for reader " + readerNumber + " from DB");

        List<Lending> lendings = new ArrayList<>();
        for (LendingSqlEntity l : lendingRepo.listOutstandingByReaderNumber(readerNumber)) {
            lendings.add(lendingEntityMapper.toModel(l));
        }

        // 3. CACHEAR NO REDIS (se houver resultados)
        if (!lendings.isEmpty()) {
            redisRepo.saveOutstandingByReader(readerNumber, lendings);
        }

        return lendings;
    }

    @Override
    public Lending save(Lending lending) {
        LendingSqlEntity entity = lendingEntityMapper.toEntity(lending);

        BookSqlEntity bookEntity = bookRepo.findSqlEntityByIsbn(
                lending.getBorrowedBook().getIsbn().toString()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Book not found with ISBN: " + lending.getBorrowedBook().getIsbn().toString()));

        ReaderDetailsSqlEntity readerEntity = readerDetailsRepo.findSqlEntityByReaderNumber(
                lending.getBorrower().getReaderNumber()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Reader not found with number: " + lending.getBorrower().getReaderNumber()));

        entity.setBook(bookEntity);
        entity.setReaderDetails(readerEntity);

        LendingSqlEntity savedEntity = lendingRepo.save(entity);
        Lending savedLending = lendingEntityMapper.toModel(savedEntity);

        // Salvar no cache individual
        redisRepo.save(savedLending);

        // Invalidar cache de outstanding do reader (pode ter mudado)
        redisRepo.invalidateOutstandingForReader(lending.getBorrower().getReaderNumber());

        return savedLending;
    }

    @Override
    public void delete(Lending lending) {
        lendingRepo.delete(lendingEntityMapper.toEntity(lending));

        // Remover do cache individual
        redisRepo.delete(lending);

        // Invalidar cache de outstanding do reader
        redisRepo.invalidateOutstandingForReader(lending.getBorrower().getReaderNumber());
    }

    // ==================== Métodos SEM cache (dados dinâmicos/estatísticas) ====================

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        // Não cachear - query específica, pouco usado
        List<Lending> lendings = new ArrayList<>();
        for (LendingSqlEntity l : lendingRepo.listByReaderNumberAndIsbn(readerNumber, isbn)) {
            lendings.add(lendingEntityMapper.toModel(l));
        }
        return lendings;
    }

    @Override
    public int getCountFromCurrentYear() {
        // Não cachear - estatística
        return lendingRepo.getCountFromCurrentYear();
    }

    @Override
    public Double getAverageDuration() {
        // Não cachear - estatística
        return lendingRepo.getAverageDuration();
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        // Não cachear - estatística
        return lendingRepo.getAvgLendingDurationByIsbn(isbn);
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        // Não cachear - muda diariamente (lendings ficam overdue a cada dia)
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingSqlEntity> cq = cb.createQuery(LendingSqlEntity.class);
        final Root<LendingSqlEntity> root = cq.from(LendingSqlEntity.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate")));

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
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned,
                                        LocalDate startDate, LocalDate endDate) {
        // Não cachear - query dinâmica com múltiplos parâmetros
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<LendingSqlEntity> cq = cb.createQuery(LendingSqlEntity.class);
        final Root<LendingSqlEntity> lendingRoot = cq.from(LendingSqlEntity.class);
        final Join<LendingSqlEntity, Book> bookJoin = lendingRoot.join("book");
        final Join<LendingSqlEntity, ReaderDetails> readerDetailsJoin = lendingRoot.join("readerDetails");
        cq.select(lendingRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(readerNumber)) {
            where.add(cb.like(readerDetailsJoin.get("readerNumber").get("readerNumber"), readerNumber));
        }
        if (StringUtils.hasText(isbn)) {
            where.add(cb.like(bookJoin.get("isbn").get("isbn"), isbn));
        }
        if (returned != null) {
            if (returned) {
                where.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            } else {
                where.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if (startDate != null) {
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        }
        if (endDate != null) {
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));
        }

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        final TypedQuery<LendingSqlEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<Lending> lendings = new ArrayList<>();
        for (LendingSqlEntity lendingEntity : q.getResultList()) {
            lendings.add(lendingEntityMapper.toModel(lendingEntity));
        }

        return lendings;
    }
}