package com.example.web.advice;

import com.example.common.model.ApiResponse;
import com.example.web.annotation.NoWrap;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 成功响应统一包装为 ApiResponse。
 * 规则：
 * - 已是 ApiResponse 或 ResponseEntity：不再包装
 * - 方法/类标注 @NoWrap：不包装
 * - 返回类型是 String 或二进制文件：不包装（避免类型转换/下载异常）
 * - Content-Type 非 JSON：不包装
 */
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
        // 如果方法或类上有 @NoWrap，则不支持包装
        if (returnType.getContainingClass().isAnnotationPresent(NoWrap.class)) return false;
        if (returnType.hasMethodAnnotation(NoWrap.class)) return false;
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        // 已经是统一结构
        if (body instanceof ApiResponse) return body;
        if (body instanceof ResponseEntity) return body;

        // 非 JSON（如 text/html、octet-stream），不包
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) return body;
        // 返回 String 时，交给原始 StringHttpMessageConverter
        if (body instanceof String) return body;

        ApiResponse<Object> resp = ApiResponse.ok(body);
        // 把 traceId 透出
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            resp.setTraceId(traceId);
        }
        return resp;
    }
}
