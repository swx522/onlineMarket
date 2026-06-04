package com.macro.mall.assistant.llm.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.llm.LlmClient;
import com.macro.mall.assistant.llm.LlmException;
import com.macro.mall.assistant.llm.LlmMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 阿里云通义千问大模型客户端，基于其 <b>OpenAI 兼容模式</b> 实现。
 *
 * <p>请求与返回结构遵循 OpenAI Chat Completions 协议，因此把 {@code baseUrl}、{@code model}、
 * {@code apiKey} 换成 OpenAI 或智谱 GLM 的对应值即可平滑切换。
 *
 * @see <a href="https://help.aliyun.com/zh/dashscope/developer-reference/compatibility-of-openai-with-dashscope">通义千问 OpenAI 兼容接口</a>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "assistant.llm.provider", havingValue = "tongyi", matchIfMissing = true)
public class TongYiLlmClient implements LlmClient {

    private final AssistantProperties properties;
    private final RestTemplate restTemplate;

    public TongYiLlmClient(AssistantProperties properties,
                           @Qualifier("llmRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public String chat(List<LlmMessage> messages) {
        AssistantProperties.Llm cfg = properties.getLlm();
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new LlmException("未配置大模型 API Key（assistant.llm.api-key / 环境变量 DASHSCOPE_API_KEY）");
        }

        ChatCompletionRequest body = new ChatCompletionRequest();
        body.setModel(cfg.getModel());
        body.setMessages(messages);
        body.setTemperature(cfg.getTemperature());
        body.setMaxTokens(cfg.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cfg.getApiKey());
        HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<ChatCompletionResponse> resp =
                    restTemplate.postForEntity(cfg.getBaseUrl(), entity, ChatCompletionResponse.class);
            ChatCompletionResponse data = resp.getBody();
            if (data == null || data.getChoices() == null || data.getChoices().isEmpty()) {
                throw new LlmException("大模型返回结果为空");
            }
            ChatCompletionResponse.Choice choice = data.getChoices().get(0);
            if (choice.getMessage() == null || !StringUtils.hasText(choice.getMessage().getContent())) {
                throw new LlmException("大模型返回内容为空");
            }
            return choice.getMessage().getContent().trim();
        } catch (RestClientException e) {
            log.error("调用通义千问失败, model={}, msg={}", cfg.getModel(), e.getMessage());
            throw new LlmException("调用大模型服务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String provider() {
        return "tongyi";
    }

    // ---------------------------------------------------------------------
    // OpenAI 兼容协议的请求 / 响应结构
    // ---------------------------------------------------------------------

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ChatCompletionRequest {
        private String model;
        private List<LlmMessage> messages;
        private Double temperature;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatCompletionResponse {
        private String id;
        private String model;
        private List<Choice> choices;
        private Usage usage;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Choice {
            private Integer index;
            private LlmMessage message;
            @JsonProperty("finish_reason")
            private String finishReason;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Usage {
            @JsonProperty("prompt_tokens")
            private Integer promptTokens;
            @JsonProperty("completion_tokens")
            private Integer completionTokens;
            @JsonProperty("total_tokens")
            private Integer totalTokens;
        }
    }
}
