package com.example.common.constant;

/**
 * 通用错误码
 */
public enum CommonErrorCodes {
    OK("OK", "success"),
    INVALID_PARAM("INVALID_PARAM", "参数不合法"),
    UNAUTHORIZED("UNAUTHORIZED", "未认证"),
    FORBIDDEN("FORBIDDEN", "无权限"),
    NOT_FOUND("NOT_FOUND", "资源不存在"),
    CONFLICT("CONFLICT", "资源冲突"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "请求过于频繁"),
    INTERNAL_ERROR("INTERNAL_ERROR", "服务器内部错误");

    private final String code;
    private final String defaultMessage;

    CommonErrorCodes(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() { return code; }
    public String defaultMessage() { return defaultMessage; }
}
