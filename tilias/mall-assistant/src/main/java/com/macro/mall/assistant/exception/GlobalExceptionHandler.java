package com.macro.mall.assistant.exception;

import com.macro.mall.assistant.api.CommonResult;
import com.macro.mall.assistant.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 智能助手模块统一异常处理。
 */
@Slf4j
@RestControllerAdvice("com.macro.mall.assistant.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public CommonResult<Void> handleRateLimit(RateLimitException e) {
        return CommonResult.failed(ResultCode.RATE_LIMITED, e.getMessage());
    }

    @ExceptionHandler(SensitiveContentException.class)
    public CommonResult<Void> handleSensitive(SensitiveContentException e) {
        return CommonResult.failed(ResultCode.SENSITIVE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValidation(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = "参数校验失败";
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validateFailed(message);
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception e) {
        log.error("智能助手接口异常", e);
        return CommonResult.failed(ResultCode.FAILED, "服务繁忙，请稍后再试");
    }
}
