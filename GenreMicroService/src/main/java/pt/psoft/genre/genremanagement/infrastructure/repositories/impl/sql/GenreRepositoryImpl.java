package pt.psoft.genre.genremanagement.infrastructure.repositories.impl.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookSqlEntity;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.mappers.GenreEntityMapper;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.redis.GenreRepositoryRedisImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreSqlEntity;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingSqlEntity;

import java.time.LocalDate;
import java.util.*;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class GenreRepositoryImpl implements GenreRepository {

    private final SpringDataGenreRepository genreRepo;
    private final GenreEntityMapper genreEntityMapper;
    private final EntityManager entityManager;
    private final GenreRepositoryRedisImpl redisRepo;  // ADICIONAR

    @Override
    public Iterable<Genre> findAll() {
        // 1. TENTAR BUSCAR DO REDIS
        Optional<List<Genre>> cached = redisRepo.getAllGenresFromRedis();

        if (cached.isPresent()) {
            System.out.println("✅ CACHE HIT - All genres from Redis");
            return cached.get();
        }

        // 2. SE NÃO EXISTIR, BUSCAR DO DB
        System.out.println("❌ CACHE MISS - All genres from DB");

        List<Genre> genres = new ArrayList<>();
        for (GenreSqlEntity g : genreRepo.findAll()) {
            genres.add(genreEntityMapper.toModel(g));
        }

        // 3. CACHEAR NO REDIS
        if (!genres.isEmpty()) {
            redisRepo.saveAllGenres(genres);
        }

        return genres;
    }

    @Override
    public Optional<Genre> findByString(String genreName) {
        // 1. TENTAR BUSCAR DO REDIS
        String redisKey = "genres:genre:" + genreName;
        Optional<Genre> cached = redisRepo.getGenreFromRedis(redisKey);

        if (cached.isPresent()) {
            System.out.println("✅ CACHE HIT - Genre '" + genreName + "' from Redis");
            return cached;
        }

        // 2. SE NÃO EXISTIR, BUSCAR DO DB
        System.out.println("❌ CACHE MISS - Genre '" + genreName + "' from DB");

        Optional<GenreSqlEntity> entityOpt = genreRepo.findByString(genreName);

        if (entityOpt.isPresent()) {
            Genre genre = genreEntityMapper.toModel(entityOpt.get());

            // 3. CACHEAR NO REDIS
            redisRepo.save(genre);

            return Optional.of(genre);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Genre save(Genre genre) {
        GenreSqlEntity entity = genreEntityMapper.toEntity(genre);
        Genre savedGenre = genreEntityMapper.toModel(genreRepo.save(entity));

        // Salvar no cache individual
        redisRepo.save(savedGenre);

        // Invalidar APENAS a lista (não os individuais!)
        redisRepo.invalidateAllGenresCache();

        return savedGenre;
    }

    @Override
    public void delete(Genre genre) {
        genreRepo.delete(genreEntityMapper.toEntity(genre));

        // Remover do cache individual
        redisRepo.delete(genre);

        // Invalidar APENAS a lista
        redisRepo.invalidateAllGenresCache();
    }

    // ==================== Métodos sem cache (estatísticas) ====================

    @Override
    public Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) {
        // Não cachear - é estatística dinâmica
        return genreRepo.findTop5GenreByBookCount(pageable);
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
        // ... código existente permanece igual ...
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<LendingSqlEntity> lendingRoot = cq.from(LendingSqlEntity.class);
        Join<LendingSqlEntity, BookSqlEntity> bookJoin = lendingRoot.join("book");
        Join<BookSqlEntity, GenreSqlEntity> genreJoin = bookJoin.join("genre");

        Expression<Integer> year = cb.function("YEAR", Integer.class, lendingRoot.get("startDate"));
        Expression<Integer> month = cb.function("MONTH", Integer.class, lendingRoot.get("startDate"));

        Expression<Long> lendingCount = cb.count(lendingRoot);

        cq.multiselect(genreJoin.get("genre"), year, month, lendingCount);
        cq.groupBy(genreJoin.get("genre"), year, month);

        LocalDate now = LocalDate.now();
        LocalDate twelveMonthsAgo = now.minusMonths(12);
        Predicate datePredicate = cb.between(lendingRoot.get("startDate"),
                java.sql.Date.valueOf(twelveMonthsAgo),
                java.sql.Date.valueOf(now));

        cq.where(datePredicate);
        cq.orderBy(cb.asc(year), cb.asc(month), cb.asc(genreJoin.get("genre")));

        TypedQuery<Tuple> query = entityManager.createQuery(cq);
        List<Tuple> results = query.getResultList();

        Map<Integer, Map<Integer, List<GenreLendingsDTO>>> groupedResults = new HashMap<>();

        for (Tuple result : results) {
            String genre = result.get(0, String.class);
            int resultYear = result.get(1, Integer.class);
            int resultMonth = result.get(2, Integer.class);
            long count = result.get(3, Long.class);
            GenreLendingsDTO genreLendingsDTO = new GenreLendingsDTO(genre, count);

            groupedResults
                    .computeIfAbsent(resultYear, k -> new HashMap<>())
                    .computeIfAbsent(resultMonth, k -> new ArrayList<>())
                    .add(genreLendingsDTO);
        }

        return getGenreLendingsPerMonthDtos(groupedResults);
    }

    @Override
    public List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page) {
        // ... código existente permanece igual ...
        int days = month.lengthOfMonth();
        LocalDate firstOfMonth = LocalDate.of(month.getYear(), month.getMonth(), 1);
        LocalDate lastOfMonth = LocalDate.of(month.getYear(), month.getMonth(), days);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GenreLendingsDTO> cq = cb.createQuery(GenreLendingsDTO.class);

        Root<LendingSqlEntity> lendingRoot = cq.from(LendingSqlEntity.class);
        Join<LendingSqlEntity, BookSqlEntity> bookJoin = lendingRoot.join("book", JoinType.LEFT);
        Join<BookSqlEntity, GenreSqlEntity> genreJoin = bookJoin.join("genre", JoinType.LEFT);

        Expression<Long> loanCount = cb.count(lendingRoot.get("pk"));
        Expression<Number> dailyAvgLoans = cb.quot(cb.toDouble(loanCount), cb.literal(days));

        cq.multiselect(genreJoin, dailyAvgLoans);
        cq.groupBy(genreJoin.get("pk"));

        Predicate startDatePredicate = cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), firstOfMonth);
        Predicate endDatePredicate = cb.lessThanOrEqualTo(lendingRoot.get("startDate"), lastOfMonth);

        Predicate finalPredicate = cb.and(startDatePredicate, endDatePredicate);

        cq.where(finalPredicate);

        final TypedQuery<GenreLendingsDTO> q = entityManager.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) {
        // ... código existente permanece igual ...
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<LendingSqlEntity> lendingRoot = cq.from(LendingSqlEntity.class);
        Join<LendingSqlEntity, BookSqlEntity> bookJoin = lendingRoot.join("book");
        Join<BookSqlEntity, GenreSqlEntity> genreJoin = bookJoin.join("genre");

        Expression<Integer> yearExpr = cb.function("YEAR", Integer.class, lendingRoot.get("startDate"));
        Expression<Integer> monthExpr = cb.function("MONTH", Integer.class, lendingRoot.get("startDate"));
        Expression<Long> lendingDurationInDays = cb.diff(lendingRoot.get("returnedDate"), lendingRoot.get("startDate"));
        double nanoSecondsInADay = 86400.0*1E9;
        Expression<Number> durationInDays = cb.quot(cb.toDouble(lendingDurationInDays),nanoSecondsInADay);
        Expression<Double> averageDuration = cb.avg(cb.toDouble(durationInDays));

        cq.multiselect(genreJoin.get("genre"), yearExpr, monthExpr, averageDuration);
        cq.groupBy(genreJoin.get("genre"), yearExpr, monthExpr);
        cq.orderBy(cb.asc(yearExpr), cb.asc(monthExpr), cb.asc(genreJoin.get("genre")));

        Predicate startDatePredicate = cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate);
        Predicate endDatePredicate = cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate);
        Predicate returnedPredicate = cb.isNotNull(lendingRoot.get("returnedDate"));
        Predicate finalPredicate = cb.and(startDatePredicate, endDatePredicate, returnedPredicate);

        cq.where(finalPredicate);

        List<Tuple> results = entityManager.createQuery(cq).getResultList();
        Map<Integer, Map<Integer, List<GenreLendingsDTO>>> groupedResults = new HashMap<>();

        for (Tuple result : results) {
            String genre = result.get(0, String.class);
            int yearValue = result.get(1, Integer.class);
            int monthValue = result.get(2, Integer.class);
            Double averageDurationValue = result.get(3, Double.class);
            GenreLendingsDTO genreLendingsDTO = new GenreLendingsDTO(genre, averageDurationValue);

            groupedResults
                    .computeIfAbsent(yearValue, k -> new HashMap<>())
                    .computeIfAbsent(monthValue, k -> new ArrayList<>())
                    .add(genreLendingsDTO);
        }

        return getGenreLendingsPerMonthDtos(groupedResults);
    }

    @NotNull
    private List<GenreLendingsPerMonthDTO> getGenreLendingsPerMonthDtos(Map<Integer, Map<Integer, List<GenreLendingsDTO>>> groupedResults) {
        List<GenreLendingsPerMonthDTO> lendingsPerMonth = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, List<GenreLendingsDTO>>> yearEntry : groupedResults.entrySet()) {
            int yearValue = yearEntry.getKey();
            for (Map.Entry<Integer, List<GenreLendingsDTO>> monthEntry : yearEntry.getValue().entrySet()) {
                int monthValue = monthEntry.getKey();
                List<GenreLendingsDTO> values = monthEntry.getValue();
                lendingsPerMonth.add(new GenreLendingsPerMonthDTO(yearValue, monthValue, values));
            }
        }

        return lendingsPerMonth;
    }
}