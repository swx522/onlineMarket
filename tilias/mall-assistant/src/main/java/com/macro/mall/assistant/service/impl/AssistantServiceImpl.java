package com.macro.mall.assistant.service.impl;

import cn.hutool.core.util.IdUtil;
import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.dto.ChatRequest;
import com.macro.mall.assistant.dto.ChatResponse;
import com.macro.mall.assistant.exception.RateLimitException;
import com.macro.mall.assistant.exception.SensitiveContentException;
import com.macro.mall.assistant.llm.LlmClient;
import com.macro.mall.assistant.llm.LlmException;
import com.macro.mall.assistant.llm.LlmMessage;
import com.macro.mall.assistant.service.AssistantService;
import com.macro.mall.assistant.service.ChatMemoryStore;
import com.macro.mall.assistant.service.RateLimiterService;
import com.macro.mall.assistant.service.SensitiveWordFilter;
import com.macro.mall.assistant.support.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能助手对话核心实现。
 *
 * <p>串联整条链路：限流校验 → 敏感词过滤 → 加载历史上下文 → 组装提示词 → 调用大模型
 * → 写回记忆 → 返回结果；当大模型不可用时执行服务降级，返回兜底话术。
 */
@Slf4j
@Service
public class AssistantServiceImpl implements AssistantService {

    private final AssistantProperties properties;
    private final LlmClient llmClient;
    private final ChatMemoryStore memoryStore;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RateLimiterService rateLimiterService;
    private final PromptBuilder promptBuilder;

    public AssistantServiceImpl(AssistantProperties properties,
                                LlmClient llmClient,
                                ChatMemoryStore memoryStore,
                                SensitiveWordFilter sensitiveWordFilter,
                                RateLimiterService rateLimiterService,
                                PromptBuilder promptBuilder) {
        this.properties = properties;
        this.llmClient = llmClient;
        this.memoryStore = memoryStore;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.rateLimiterService = rateLimiterService;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public ChatResponse chat(ChatRequest request, String clientKey) {
        String sessionId = StringUtils.hasText(request.getSessionId())
                ? request.getSessionId()
                : "sess-" + IdUtil.simpleUUID();

        // 助手整体关闭：直接降级
        if (!properties.isEnabled()) {
            return fallback(sessionId);
        }

        // 1. 限流：优先按 userId，其次按调用方（IP）
        String rateKey = StringUtils.hasText(request.getUserId()) ? request.getUserId() : clientKey;
        if (!rateLimiterService.tryAcquire(rateKey)) {
            throw new RateLimitException("提问太频繁啦，请稍等一分钟再试～");
        }

        // 2. 敏感词过滤（输入）
        if (sensitiveWordFilter.contains(request.getMessage())) {
            throw new SensitiveContentException("您的提问包含不被支持的内容，请调整后再试～");
        }

        // 3. 加载历史上下文（最近 N 轮）
        List<LlmMessage> history = memoryStore.getHistory(sessionId);

        // 4. 组装消息：system 提示词 + 历史 + 本次用户输入
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(promptBuilder.systemPrompt()));
        messages.addAll(history);
        LlmMessage userMessage = LlmMessage.user(request.getMessage());
        messages.add(userMessage);

        // 5. 调用大模型；失败则降级
        String reply;
        try {
            reply = llmClient.chat(messages);
        } catch (LlmException e) {
            log.warn("大模型调用失败，执行降级。sessionId={}, reason={}", sessionId, e.getMessage());
            return fallback(sessionId);
        }

        // 6. 输出兜底脱敏
        reply = sensitiveWordFilter.filter(reply);

        // 7. 写回记忆
        memoryStore.append(sessionId, userMessage, LlmMessage.assistant(reply));

        return ChatResponse.builder()
                .sessionId(sessionId)
                .reply(reply)
                .model(properties.getLlm().getModel())
                .fallback(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public void clearHistory(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            memoryStore.clear(sessionId);
        }
    }

    private ChatResponse fallback(String sessionId) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .reply(properties.getFallbackReply())
                .model(properties.getLlm().getModel())
                .fallback(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
