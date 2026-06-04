package com.macro.mall.assistant.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.llm.LlmMessage;
import com.macro.mall.assistant.service.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 Redis List 的对话记忆实现，适合多实例部署共享上下文。
 *
 * <p>通过 {@code assistant.memory.type=redis} 启用，依赖已配置的 Redis 连接。
 * 每个会话对应一个 Redis List，写入后做 LTRIM 截断并刷新 TTL。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "assistant.memory.type", havingValue = "redis")
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "assistant:chat:history:";

    private final StringRedisTemplate redisTemplate;
    private final AssistantProperties properties;
    private final ObjectMapper objectMapper;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate,
                                AssistantProperties properties,
                                ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    @Override
    public List<LlmMessage> getHistory(String sessionId) {
        List<String> raw = redisTemplate.opsForList().range(key(sessionId), 0, -1);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<LlmMessage> result = new ArrayList<>(raw.size());
        for (String json : raw) {
            try {
                result.add(objectMapper.readValue(json, LlmMessage.class));
            } catch (Exception e) {
                log.warn("反序列化历史消息失败，已跳过: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    public void append(String sessionId, LlmMessage userMessage, LlmMessage assistantMessage) {
        String key = key(sessionId);
        try {
            redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(userMessage));
            redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(assistantMessage));
            int maxMessages = Math.max(1, properties.getMemory().getMaxRounds()) * 2;
            // 只保留最近 maxMessages 条
            redisTemplate.opsForList().trim(key, -maxMessages, -1);
            redisTemplate.expire(key, Duration.ofSeconds(properties.getMemory().getTtlSeconds()));
        } catch (Exception e) {
            log.error("写入会话历史到 Redis 失败: {}", e.getMessage());
        }
    }

    @Override
    public void clear(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }
}
