package com.coinly.common.domain;

/**
 * 统一 API 响应封装。
 * 所有 Controller 返回值必须使用本类包装，保证前端拿到统一结构：
 * @param <T> data 字段的实际类型
 */
public class CommonResponse<T> {

    /** 业务状态码：200=成功，400=参数/业务错误，401=未授权，500=服务器错误 */
    private final int code;

    /** 提示信息，成功为 "success"，失败为具体原因 */
    private final String message;

    /** 业务数据，失败时为 null */
    private final T data;

    private CommonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（默认 message = "success"）。
     *
     * @param data 业务数据
     * @return 包装后的统一响应
     */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "success", data);
    }

    /**
     * 成功响应（自定义 message）。
     *
     * @param message 提示信息，例如 "修改成功"
     * @param data    业务数据，无数据时传 null
     */
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(200, message, data);
    }

    /**
     * 失败响应（默认 code = 500）。
     * 建议仅在不确定错误类型时使用；业务异常优先用 {@link #fail(int, String)}。
     *
     * @param message 失败原因
     */
    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(500, message, null);
    }

    /**
     * 失败响应（自定义 code）。
     *
     * @param code    业务状态码，例如 400 / 401
     * @param message 失败原因
     */
    public static <T> CommonResponse<T> fail(int code, String message) {
        return new CommonResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}