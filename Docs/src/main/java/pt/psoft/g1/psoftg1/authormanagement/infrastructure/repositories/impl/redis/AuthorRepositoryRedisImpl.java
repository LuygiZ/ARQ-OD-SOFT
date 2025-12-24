package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.mappers.AuthorRedisMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.redis.AuthorRedisDTO;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class AuthorRepositoryRedisImpl {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final AuthorRedisMapper redisMapper;

    private static final String PREFIX = "authors:author:";

    public Optional<Author> getAuthorFromRedis(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);

            if (obj == null) {
                return Optional.empty();
            }

            // Se for String JSON, deserializar
            if (obj instanceof String) {
                AuthorRedisDTO dto = mapper.readValue((String) obj, AuthorRedisDTO.class);
                return Optional.of(redisMapper.toDomain(dto));
            }

            // Se for o objeto direto
            if (obj instanceof AuthorRedisDTO) {
                return Optional.of(redisMapper.toDomain((AuthorRedisDTO) obj));
            }

            System.err.println("❗ Unexpected Redis object type: " + obj.getClass());
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void save(Author author) {
        try {
            AuthorRedisDTO dto = redisMapper.toDTO(author);
            String json = mapper.writeValueAsString(dto);
            String key = PREFIX + author.getAuthorNumber();

            redisTemplate.opsForValue().set(key, json);
            System.out.println("💾 Saved to Redis: " + key);

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save author to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Author author) {
        String key = PREFIX + author.getAuthorNumber();
        redisTemplate.delete(key);
        System.out.println("🗑️ Deleted from Redis: " + key);
    }

    private void invalidateCacheForAuthor() {
        deleteKeysByPattern("authors:*");
    }

    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}