package com.example.common.exception;

/**
 * 业务异常：携带错误码与消息
 */
public class BizException extends RuntimeException {
    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() { return code; }

    public static BizException of(String code, String message) {
        return new BizException(code, message);
    }
}
