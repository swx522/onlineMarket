package com.macro.mall.assistant.service;

import com.macro.mall.assistant.config.AssistantProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的进程内固定窗口限流器：限制「单用户每分钟最多 N 次」。
 *
 * <p>以 userId（或会话/IP）为维度，每分钟一个窗口。实现轻量、零外部依赖，
 * 适合单机部署；多实例场景建议替换为基于 Redis 的分布式限流。
 */
@Component
public class RateLimiterService {

    private static final long WINDOW_MILLIS = 60_000L;

    private final AssistantProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiterService(AssistantProperties properties) {
        this.properties = properties;
    }

    /**
     * 尝试获取一次调用许可。
     *
     * @param key 限流维度标识（用户ID / 会话ID / IP）
     * @return true=允许通过，false=已超过阈值需拒绝
     */
    public boolean tryAcquire(String key) {
        if (!properties.getRateLimit().isEnabled()) {
            return true;
        }
        int max = properties.getRateLimit().getMaxPerMinute();
        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(key, k -> new Window(now));
        synchronized (window) {
            if (now - window.startMillis >= WINDOW_MILLIS) {
                window.startMillis = now;
                window.count.set(0);
            }
            if (window.count.get() >= max) {
                return false;
            }
            window.count.incrementAndGet();
            return true;
        }
    }

    private static class Window {
        volatile long startMillis;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
