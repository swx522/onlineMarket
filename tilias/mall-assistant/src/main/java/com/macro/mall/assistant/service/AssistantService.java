package com.macro.mall.assistant.service;

import com.macro.mall.assistant.dto.ChatRequest;
import com.macro.mall.assistant.dto.ChatResponse;

/**
 * 智能助手对话服务。
 */
public interface AssistantService {

    /**
     * 处理一次对话：限流 -> 敏感词校验 -> 拼接上下文与提示词 -> 调用大模型 -> 落库记忆 -> 返回。
     *
     * @param request   对话请求
     * @param clientKey 限流兜底维度（通常是客户端 IP，当未传 userId 时使用）
     * @return 助手回复
     */
    ChatResponse chat(ChatRequest request, String clientKey);

    /**
     * 清空某个会话的上下文历史。
     */
    void clearHistory(String sessionId);
}
