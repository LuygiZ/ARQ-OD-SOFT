package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.mappers.LendingRedisMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.redis.LendingRedisDTO;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LendingRepositoryRedisImpl {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final LendingRedisMapper redisMapper;

    private static final String PREFIX_LENDING = "lendings:number:";
    private static final String PREFIX_OUTSTANDING = "lendings:outstanding:";

    // ==================== CACHE INDIVIDUAL (por lendingNumber) ====================

    public Optional<Lending> getLendingFromRedis(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);

            if (obj == null) {
                return Optional.empty();
            }

            if (obj instanceof String) {
                LendingRedisDTO dto = mapper.readValue((String) obj, LendingRedisDTO.class);
                return Optional.of(redisMapper.toDomain(dto));
            }

            if (obj instanceof LendingRedisDTO) {
                return Optional.of(redisMapper.toDomain((LendingRedisDTO) obj));
            }

            System.err.println("❗ Unexpected Redis object type: " + obj.getClass());
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading lending from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void save(Lending lending) {
        try {
            LendingRedisDTO dto = redisMapper.toDTO(lending);
            String json = mapper.writeValueAsString(dto);
            String key = PREFIX_LENDING + lending.getLendingNumber();

            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10));
            System.out.println("💾 Saved lending to Redis: " + key);

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save lending to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Lending lending) {
        String key = PREFIX_LENDING + lending.getLendingNumber();
        redisTemplate.delete(key);
        System.out.println("🗑️ Deleted lending from Redis: " + key);
    }

    // ==================== CACHE DE LISTAS (outstanding por reader) ====================

    public Optional<List<Lending>> getOutstandingByReaderFromRedis(String readerNumber) {
        try {
            String key = PREFIX_OUTSTANDING + readerNumber;
            Object obj = redisTemplate.opsForValue().get(key);

            if (obj == null) {
                return Optional.empty();
            }

            if (obj instanceof String) {
                List<LendingRedisDTO> dtos = mapper.readValue(
                        (String) obj,
                        new TypeReference<List<LendingRedisDTO>>() {}
                );

                List<Lending> lendings = dtos.stream()
                        .map(redisMapper::toDomain)
                        .collect(Collectors.toList());

                return Optional.of(lendings);
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading outstanding lendings from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void saveOutstandingByReader(String readerNumber, List<Lending> lendings) {
        try {
            List<LendingRedisDTO> dtos = lendings.stream()
                    .map(redisMapper::toDTO)
                    .collect(Collectors.toList());

            String json = mapper.writeValueAsString(dtos);
            String key = PREFIX_OUTSTANDING + readerNumber;

            // TTL de 5 minutos (mais curto - dados mudam frequentemente)
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(5));

            System.out.println("💾 Saved outstanding lendings to Redis: " + key + " (" + lendings.size() + " lendings)");

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save outstanding lendings to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== INVALIDAÇÃO ====================

    public void invalidateOutstandingCaches() {
        System.out.println("🧹 Invalidating all outstanding lendings caches...");
        deleteKeysByPattern(PREFIX_OUTSTANDING + "*");
    }

    public void invalidateOutstandingForReader(String readerNumber) {
        String key = PREFIX_OUTSTANDING + readerNumber;
        redisTemplate.delete(key);
        System.out.println("🗑️ Invalidated outstanding lendings for reader: " + readerNumber);
    }

    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("🗑️ Deleted " + keys.size() + " keys matching: " + pattern);
        }
    }
}