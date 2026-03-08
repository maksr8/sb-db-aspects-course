package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "rate_limit:user:";

    public boolean isActionAllowed(String userId, int maxRequests, int timeWindowInSeconds) {
        String key = KEY_PREFIX + userId;

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(timeWindowInSeconds));
        }

        return currentRequests != null && currentRequests <= maxRequests;
    }
}