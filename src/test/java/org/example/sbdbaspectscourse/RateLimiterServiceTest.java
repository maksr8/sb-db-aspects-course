package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.service.RateLimiterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(RateLimiterService.class)
class RateLimiterServiceTest extends AbstractRedisTestcontainersSetupTest {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    void testRateLimiterBlocksRequestsOverLimit() {
        String userId = "user_limit_test";
        int maxRequests = 3;
        int timeWindow = 10;

        Assertions.assertTrue(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));
        Assertions.assertTrue(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));
        Assertions.assertTrue(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));

        Assertions.assertFalse(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));
    }

    @Test
    void testRateLimiterResetsAfterTtlExpired() throws InterruptedException {
        String userId = "user_ttl_test";
        int maxRequests = 1;
        int timeWindow = 1;

        Assertions.assertTrue(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));

        Assertions.assertFalse(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));

        Thread.sleep(1100);

        Assertions.assertTrue(rateLimiterService.isActionAllowed(userId, maxRequests, timeWindow));
    }
}