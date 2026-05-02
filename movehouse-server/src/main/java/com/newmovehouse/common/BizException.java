package com.newmovehouse.common;

/**
 * 可预期的业务异常，由全局 {@code @RestControllerAdvice} 映射为 HTTP 400 与友好提示。
 */
public class BizException extends RuntimeException {

    /**
     * @param message 面向用户或前端的错误说明
     */
    public BizException(String message) {
        super(message);
    }
}
