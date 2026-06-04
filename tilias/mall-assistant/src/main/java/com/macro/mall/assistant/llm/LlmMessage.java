package com.macro.mall.assistant.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型对话中的一条消息（OpenAI 兼容格式）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {

    /** 系统设定（人设、规则） */
    public static final String ROLE_SYSTEM = "system";
    /** 用户消息 */
    public static final String ROLE_USER = "user";
    /** 助手回复 */
    public static final String ROLE_ASSISTANT = "assistant";

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    public static LlmMessage system(String content) {
        return new LlmMessage(ROLE_SYSTEM, content);
    }

    public static LlmMessage user(String content) {
        return new LlmMessage(ROLE_USER, content);
    }

    public static LlmMessage assistant(String content) {
        return new LlmMessage(ROLE_ASSISTANT, content);
    }
}
