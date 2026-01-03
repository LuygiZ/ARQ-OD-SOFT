package pt.psoft.book.bookmanagement.infrastructure.repositories.impl.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.mappers.BookRedisMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.redis.BookRedisDTO;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class BookRepositoryRedisImpl {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final BookRedisMapper redisMapper;

    private static final String PREFIX = "books:isbn:";

    public Optional<Book> getBookFromRedis(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);

            if (obj == null) {
                return Optional.empty();
            }

            // Se for String JSON, deserializar
            if (obj instanceof String) {
                BookRedisDTO dto = mapper.readValue((String) obj, BookRedisDTO.class);
                return Optional.of(redisMapper.toDomain(dto));
            }

            // Se for o objeto direto
            if (obj instanceof BookRedisDTO) {
                return Optional.of(redisMapper.toDomain((BookRedisDTO) obj));
            }

            System.err.println("❗ Unexpected Redis object type: " + obj.getClass());
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❗ Error reading book from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void save(Book book) {
        try {
            BookRedisDTO dto = redisMapper.toDTO(book);
            String json = mapper.writeValueAsString(dto);
            String key = PREFIX + book.getIsbn().toString();

            redisTemplate.opsForValue().set(key, json);
            System.out.println("💾 Saved book to Redis: " + key);

        } catch (JsonProcessingException e) {
            System.err.println("❗ Failed to save book to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Book book) {
        String key = PREFIX + book.getIsbn().toString();
        redisTemplate.delete(key);
        System.out.println("🗑️ Deleted book from Redis: " + key);
    }

    private void invalidateCacheForBooks() {
        deleteKeysByPattern("books:*");
    }

    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}