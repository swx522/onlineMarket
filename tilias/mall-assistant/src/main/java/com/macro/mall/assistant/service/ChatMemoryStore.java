package com.macro.mall.assistant.service;

import com.macro.mall.assistant.llm.LlmMessage;

import java.util.List;

/**
 * 对话上下文记忆存储。
 *
 * <p>负责保存每个会话最近若干轮的问答，使助手具备"记忆"。
 * 提供两种实现：进程内存（默认）与 Redis（适合多实例部署）。
 */
public interface ChatMemoryStore {

    /**
     * 获取指定会话的历史消息（按时间正序，已截断到最近 N 轮）。
     */
    List<LlmMessage> getHistory(String sessionId);

    /**
     * 追加一轮问答（用户消息 + 助手回复），并自动按配置的最大轮数截断。
     */
    void append(String sessionId, LlmMessage userMessage, LlmMessage assistantMessage);

    /**
     * 清空指定会话的历史。
     */
    void clear(String sessionId);
}
