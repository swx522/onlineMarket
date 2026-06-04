package com.macro.mall.assistant.exception;

/**
 * 用户输入命中敏感词时抛出。
 */
public class SensitiveContentException extends RuntimeException {
    public SensitiveContentException(String message) {
        super(message);
    }
}
