package com.newmovehouse.common;

public class ApiResponse<T> {
    public int code;
    public String message;
    public T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "ok", null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(500, message, null);
    }
}

