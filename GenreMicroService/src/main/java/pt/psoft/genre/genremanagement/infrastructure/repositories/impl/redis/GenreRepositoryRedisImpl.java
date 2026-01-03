package pt.psoft.genre.genremanagement.infrastructure.repositories.impl.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.mappers.GenreRedisMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.redis.GenreRedisDTO;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreRepositoryRedisImpl {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final GenreRedisMapper redisMapper;

    private static final String PREFIX_GENRE = "genres:genre:";
    private static final String KEY_ALL_GENRES = "genres:all";

    // ==================== CACHE INDIVIDUAL (por nome de genre) ====================

    public Optional<Genre> getGenreFromRedis(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);

            if (obj == null) {
                return Optional.empty();
            }

            // Se for String JSON, deserializar
            if (obj instanceof String) {
                GenreRedisDTO dto = mapper.readValue((String) obj, GenreRedisDTO.class);
                return Optional.of(redisMapper.toDomain(dto));
            }

            // Se for o objeto direto
            if (obj instanceof GenreRedisDTO) {
                return Optional.of(redisMapper.toDomain((GenreRedisDTO) obj));
            }

            System.err.println("❗ Unexpected Redis object type: " + obj.getClass());
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading genre from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void save(Genre genre) {
        try {
            GenreRedisDTO dto = redisMapper.toDTO(genre);
            String json = mapper.writeValueAsString(dto);
            String key = PREFIX_GENRE + genre.getGenre();

            redisTemplate.opsForValue().set(key, json);
            System.out.println("💾 Saved genre to Redis: " + key);

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save genre to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Genre genre) {
        String key = PREFIX_GENRE + genre.getGenre();
        redisTemplate.delete(key);
        System.out.println("🗑️ Deleted genre from Redis: " + key);
    }

    // ==================== CACHE DE LISTA (findAll) ====================

    public Optional<List<Genre>> getAllGenresFromRedis() {
        try {
            Object obj = redisTemplate.opsForValue().get(KEY_ALL_GENRES);

            if (obj == null) {
                return Optional.empty();
            }

            if (obj instanceof String) {
                List<GenreRedisDTO> dtos = mapper.readValue(
                        (String) obj,
                        new TypeReference<List<GenreRedisDTO>>() {}
                );

                List<Genre> genres = dtos.stream()
                        .map(redisMapper::toDomain)
                        .collect(Collectors.toList());

                return Optional.of(genres);
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading all genres from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void saveAllGenres(List<Genre> genres) {
        try {
            List<GenreRedisDTO> dtos = genres.stream()
                    .map(redisMapper::toDTO)
                    .collect(Collectors.toList());

            String json = mapper.writeValueAsString(dtos);

            // TTL de 10 minutos para lista de genres
            redisTemplate.opsForValue().set(KEY_ALL_GENRES, json, Duration.ofMinutes(10));

            System.out.println("💾 Saved all genres to Redis: " + genres.size() + " genres");

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save all genres to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== INVALIDAÇÃO ====================

    public void invalidateAllGenresCache() {
        System.out.println("🧹 Invalidating genres list cache...");

        // Apagar APENAS a lista completa, NÃO os genres individuais
        redisTemplate.delete(KEY_ALL_GENRES);

        System.out.println("🗑️ Deleted key: " + KEY_ALL_GENRES);
    }

    // Métudo separado para invalidar TUDO (inclusive individuais)
    public void invalidateEverything() {
        System.out.println("🧹 Invalidating ALL genres cache (including individual)...");

        // Apagar lista completa
        redisTemplate.delete(KEY_ALL_GENRES);

        // Apagar também genres individuais
        deleteKeysByPattern(PREFIX_GENRE + "*");
    }

    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("🗑️ Deleted " + keys.size() + " keys matching: " + pattern);
        }
    }
}