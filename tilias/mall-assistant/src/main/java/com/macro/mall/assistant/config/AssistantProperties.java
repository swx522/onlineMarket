package com.macro.mall.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能助手相关配置项，统一以 {@code assistant.*} 前缀维护在 application.yml 中。
 *
 * <p>API Key 等敏感信息建议通过环境变量注入，切勿硬编码或暴露到前端。
 */
@Data
@ConfigurationProperties(prefix = "assistant")
public class AssistantProperties {

    /**
     * 是否启用智能助手，关闭后接口直接返回降级提示。
     */
    private boolean enabled = true;

    /**
     * 降级话术：当 LLM 不可用或助手被关闭时返回给用户的提示。
     */
    private String fallbackReply = "智能助手暂时打了个盹儿，请稍后再试，或联系人工客服～";

    /**
     * 大模型相关配置。
     */
    private Llm llm = new Llm();

    /**
     * 对话上下文记忆配置。
     */
    private Memory memory = new Memory();

    /**
     * 限流配置。
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 敏感词配置。
     */
    private Sensitive sensitive = new Sensitive();

    @Data
    public static class Llm {
        /**
         * 服务商标识，目前内置 tongyi（阿里云通义千问，OpenAI 兼容协议）。
         */
        private String provider = "tongyi";

        /**
         * 大模型对话接口地址。默认走通义千问的 OpenAI 兼容模式，
         * 想切换到 OpenAI / 智谱 GLM 只需改 baseUrl + model + apiKey。
         */
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        /**
         * API Key，强烈建议用环境变量注入：${DASHSCOPE_API_KEY}
         */
        private String apiKey = "";

        /**
         * 模型名称，例如 qwen-plus / qwen-turbo / qwen-max。
         */
        private String model = "qwen-plus";

        /**
         * 采样温度，0~2，越大越发散。
         */
        private Double temperature = 0.7;

        /**
         * 单次回复最大 token 数。
         */
        private Integer maxTokens = 1024;

        /**
         * 连接 / 读取超时时间（毫秒）。
         */
        private int timeoutMillis = 30000;

        /**
         * 系统提示词；为空时使用 PromptBuilder 中的默认电商客服人设。
         */
        private String systemPrompt = "";
    }

    @Data
    public static class Memory {
        /**
         * 记忆存储方式：memory（默认，进程内）或 redis。
         */
        private String type = "memory";

        /**
         * 保留最近的对话轮数（1 轮 = 1 问 1 答）。
         */
        private int maxRounds = 5;

        /**
         * 会话空闲多久后过期（秒），仅 redis 模式生效。
         */
        private long ttlSeconds = 1800;
    }

    @Data
    public static class RateLimit {
        /**
         * 是否启用限流。
         */
        private boolean enabled = true;

        /**
         * 单用户每分钟最多调用次数。
         */
        private int maxPerMinute = 10;
    }

    @Data
    public static class Sensitive {
        /**
         * 是否启用敏感词过滤。
         */
        private boolean enabled = true;

        /**
         * 自定义敏感词（会与内置词库合并）。
         */
        private List<String> words = new ArrayList<>();
    }
}
