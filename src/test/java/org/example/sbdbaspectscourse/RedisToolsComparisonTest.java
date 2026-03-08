package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.model.redis.DriverSession;
import org.example.sbdbaspectscourse.repository.redis.DriverSessionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

class RedisToolsComparisonTest extends AbstractRedisTestcontainersSetupTest {

    @Autowired
    private DriverSessionRepository driverSessionRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testCrudRepositoryMethods() {
        DriverSession session = new DriverSession("sess-1", "driver-99", "192.168.0.1");

        driverSessionRepository.save(session);

        Assertions.assertTrue(driverSessionRepository.findById("sess-1").isPresent());

        long count = driverSessionRepository.count();
        Assertions.assertEquals(1L, count);

        driverSessionRepository.deleteById("sess-1");
        Assertions.assertFalse(driverSessionRepository.existsById("sess-1"));
    }

    @Test
    void testRedisTemplateMethods() {
        String key = "raw_template_key";
        String value = "raw_value";

        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(5));

        Assertions.assertEquals(Boolean.TRUE, redisTemplate.hasKey(key));

        String retrievedValue = redisTemplate.opsForValue().get(key);
        Assertions.assertEquals(value, retrievedValue);

        Long expireTime = redisTemplate.getExpire(key);
        Assertions.assertNotNull(expireTime);
        Assertions.assertTrue(expireTime > 0);

        redisTemplate.delete(key);
    }
}