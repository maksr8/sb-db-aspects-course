package org.example.sbdbaspectscourse.dao;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.model.redis.ActiveTrip;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ActiveTripRedisDao {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "active_trip:";

    public void save(ActiveTrip trip) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String key = KEY_PREFIX + trip.getId();

        Map<String, String> tripMap = new HashMap<>();
        tripMap.put("id", trip.getId());
        tripMap.put("driverId", trip.getDriverId());
        tripMap.put("passengerId", trip.getPassengerId());
        tripMap.put("status", trip.getStatus());

        hashOps.putAll(key, tripMap);
    }

    public Optional<ActiveTrip> findById(String id) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String key = KEY_PREFIX + id;

        Map<String, String> entries = hashOps.entries(key);

        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        ActiveTrip trip = new ActiveTrip(
                entries.get("id"),
                entries.get("driverId"),
                entries.get("passengerId"),
                entries.get("status")
        );

        return Optional.of(trip);
    }

    public void update(ActiveTrip trip) {
        if (!existsById(trip.getId())) {
            throw new RuntimeException("Trip not found for update");
        }
        save(trip);
    }

    public List<ActiveTrip> findAll() {
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

        List<ActiveTrip> trips = new ArrayList<>();
        for (String key : keys) {
            String id = key.replace(KEY_PREFIX, "");
            findById(id).ifPresent(trips::add);
        }
        return trips;
    }

    public void deleteById(String id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

    public boolean existsById(String id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + id));
    }

    public void patch(String id, Map<String, String> updates) {
        if (!existsById(id)) {
            throw new RuntimeException("Trip not found for patching");
        }
        redisTemplate.opsForHash().putAll(KEY_PREFIX + id, updates);
    }
}