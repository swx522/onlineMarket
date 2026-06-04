package com.macro.mall.assistant.llm;

/**
 * 调用大模型过程中发生的异常（网络超时、鉴权失败、返回结构异常等）。
 * 上层会捕获该异常并执行降级逻辑。
 */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
