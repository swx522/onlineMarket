package com.macro.mall.assistant;

import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.service.RateLimiterService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 限流器单元测试（纯逻辑，无需 Spring 容器）。
 */
class RateLimiterServiceTest {

    private RateLimiterService newLimiter(boolean enabled, int maxPerMinute) {
        AssistantProperties props = new AssistantProperties();
        props.getRateLimit().setEnabled(enabled);
        props.getRateLimit().setMaxPerMinute(maxPerMinute);
        return new RateLimiterService(props);
    }

    @Test
    void shouldAllowWithinLimitAndRejectBeyond() {
        RateLimiterService limiter = newLimiter(true, 3);
        assertTrue(limiter.tryAcquire("user-1"));
        assertTrue(limiter.tryAcquire("user-1"));
        assertTrue(limiter.tryAcquire("user-1"));
        // 第 4 次超过阈值，应被拒绝
        assertFalse(limiter.tryAcquire("user-1"));
    }

    @Test
    void limitShouldBeIsolatedPerKey() {
        RateLimiterService limiter = newLimiter(true, 1);
        assertTrue(limiter.tryAcquire("user-a"));
        assertFalse(limiter.tryAcquire("user-a"));
        // 不同用户互不影响
        assertTrue(limiter.tryAcquire("user-b"));
    }

    @Test
    void disabledLimiterShouldAlwaysAllow() {
        RateLimiterService limiter = newLimiter(false, 1);
        for (int i = 0; i < 100; i++) {
            assertTrue(limiter.tryAcquire("user-x"));
        }
    }
}
