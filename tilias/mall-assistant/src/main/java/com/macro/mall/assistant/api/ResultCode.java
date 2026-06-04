package com.macro.mall.assistant.api;

/**
 * 通用 API 返回码。
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    /** 触发限流 */
    RATE_LIMITED(429, "请求过于频繁，请稍后再试"),
    /** 命中敏感词 */
    SENSITIVE_CONTENT(460, "内容包含敏感词，已被拦截"),
    /** 下游 LLM 服务不可用 */
    LLM_UNAVAILABLE(461, "智能助手服务暂时不可用，请稍后再试");

    private final long code;
    private final String message;

    ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public long getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
