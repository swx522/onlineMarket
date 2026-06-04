package com.macro.mall.assistant.llm;

import java.util.List;

/**
 * 统一的大模型对话接口。
 *
 * <p>对上层业务屏蔽具体服务商差异，更换 LLM 供应商（通义千问 / 智谱 GLM / OpenAI）时，
 * 只需提供一个新的实现并调整配置，业务代码无需改动。
 */
public interface LlmClient {

    /**
     * 发送一组消息并获取模型回复。
     *
     * @param messages 完整的对话消息列表（含 system 提示词、历史上下文与本次用户输入）
     * @return 模型生成的回复文本
     * @throws LlmException 当下游服务异常、超时或返回结构非法时抛出
     */
    String chat(List<LlmMessage> messages);

    /**
     * 当前实现对应的服务商标识，例如 tongyi。
     */
    String provider();
}
