package com.coinly.common.domain;


public class CommonResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private CommonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "success", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(200, message, data);
    }

    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(500, message, null);
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
