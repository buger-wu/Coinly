package com.coinly.common.exception;

/**
 * 业务异常。
 * 用于在 Service / Controller 层主动抛出的可预期业务错误，
 * @see GlobalExceptionHandler
 */
public class BusinessException extends RuntimeException {

    /** 业务状态码：默认 400，401 表示未授权 */
    private final int code;

    /**
     * 构造业务异常（默认 code = 400）。
     *
     * @param message 错误信息，会直接返回给前端
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造业务异常（自定义 code）。
     *
     * @param code    业务状态码，例如 401（未授权）、403（禁止访问）、404（资源不存在）
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}