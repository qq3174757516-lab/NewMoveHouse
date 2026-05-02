package com.newmovehouse.common;

/**
 * 统一 REST 响应体：{@code code=0} 表示成功，非 0 表示失败并携带 {@link #message}。
 *
 * @param <T> 响应体 {@link #data} 的类型参数
 */
public class ApiResponse<T> {
    /** 业务状态码，0 为成功 */
    public int code;
    /** 提示信息 */
    public String message;
    /** 业务数据 */
    public T data;

    /** 无参构造，供 JSON 反序列化使用 */
    public ApiResponse() {
    }

    /**
     * 全字段构造。
     *
     * @param code    状态码
     * @param message 提示
     * @param data    数据
     */
    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应并携带数据。
     *
     * @param data 载荷
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    /** 成功且无返回体 */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "ok", null);
    }

    /**
     * 失败响应。
     *
     * @param message 错误说明
     */
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
