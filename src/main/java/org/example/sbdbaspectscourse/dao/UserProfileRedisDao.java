package org.example.sbdbaspectscourse.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.model.redis.UserProfile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserProfileRedisDao {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "user:profile:";

    public void save(UserProfile profile) {
        try {
            String jsonValue = objectMapper.writeValueAsString(profile);
            redisTemplate.opsForValue().set(KEY_PREFIX + profile.getId(), jsonValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    public Optional<UserProfile> findById(String id) {
        String jsonValue = redisTemplate.opsForValue().get(KEY_PREFIX + id);
        if (jsonValue == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(jsonValue, UserProfile.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public void update(UserProfile profile) {
        if (!existsById(profile.getId())) {
            throw new RuntimeException("Profile not found for update");
        }
        save(profile);
    }

    public List<UserProfile> findAll() {
        List<String> keys = new ArrayList<>();

        redisTemplate.execute((RedisConnection connection) -> {
            ScanOptions options = ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(50).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
            return null;
        });

        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> jsonValues = redisTemplate.opsForValue().multiGet(keys);
        List<UserProfile> profiles = new ArrayList<>();

        if (jsonValues != null) {
            for (String json : jsonValues) {
                if (json != null) {
                    try {
                        profiles.add(objectMapper.readValue(json, UserProfile.class));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Deserialization failed", e);
                    }
                }
            }
        }
        return profiles;
    }

    public void deleteById(String id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

    public boolean existsById(String id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + id));
    }
}