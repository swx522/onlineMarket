package com.macro.mall.assistant.api;

/**
 * 错误码接口，便于统一封装返回结果。
 *
 * <p>与 mall-common 中的同名接口保持一致，方便后续并入主工程时直接复用。
 */
public interface IErrorCode {

    /**
     * 错误码
     */
    long getCode();

    /**
     * 错误提示信息
     */
    String getMessage();
}
