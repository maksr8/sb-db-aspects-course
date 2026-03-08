package org.example.sbdbaspectscourse;

import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@DataRedisTest
public abstract class AbstractRedisTestcontainersSetupTest {

    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.6")
            .withExposedPorts(6379);

    static {
        redisContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }
}