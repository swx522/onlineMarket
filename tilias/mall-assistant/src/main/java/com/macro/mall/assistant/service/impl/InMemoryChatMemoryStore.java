package com.macro.mall.assistant.service.impl;

import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.llm.LlmMessage;
import com.macro.mall.assistant.service.ChatMemoryStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 进程内存版对话记忆（默认实现）。
 *
 * <p>使用带容量上限的 LRU Map 防止内存无限增长（超出后淘汰最久未使用的会话）。
 * 单机部署足够；多实例部署请切换到 {@link RedisChatMemoryStore}。
 */
@Component
@ConditionalOnProperty(name = "assistant.memory.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryChatMemoryStore implements ChatMemoryStore {

    /** 最多保留的活跃会话数量，超出后按 LRU 淘汰 */
    private static final int MAX_SESSIONS = 10000;

    private final AssistantProperties properties;

    private final Map<String, LinkedList<LlmMessage>> store =
            Collections.synchronizedMap(new java.util.LinkedHashMap<String, LinkedList<LlmMessage>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, LinkedList<LlmMessage>> eldest) {
                    return size() > MAX_SESSIONS;
                }
            });

    public InMemoryChatMemoryStore(AssistantProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<LlmMessage> getHistory(String sessionId) {
        LinkedList<LlmMessage> history = store.get(sessionId);
        if (history == null) {
            return Collections.emptyList();
        }
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    @Override
    public void append(String sessionId, LlmMessage userMessage, LlmMessage assistantMessage) {
        LinkedList<LlmMessage> history;
        // synchronizedMap 的 computeIfAbsent 默认方法在 JDK8 下并非原子，这里显式加锁保证原子性
        synchronized (store) {
            history = store.computeIfAbsent(sessionId, k -> new LinkedList<>());
        }
        int maxMessages = Math.max(1, properties.getMemory().getMaxRounds()) * 2;
        synchronized (history) {
            history.addLast(userMessage);
            history.addLast(assistantMessage);
            while (history.size() > maxMessages) {
                history.removeFirst();
            }
        }
    }

    @Override
    public void clear(String sessionId) {
        store.remove(sessionId);
    }
}
