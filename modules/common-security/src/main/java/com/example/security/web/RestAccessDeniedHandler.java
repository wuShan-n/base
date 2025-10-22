package com.example.security.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 403 统一输出（避免默认跳转）
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = String.format(
                "{\"code\":\"FORBIDDEN\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
                safeMsg(accessDeniedException.getMessage()),
                Instant.now().toString(),
                request.getRequestURI()
        );
        response.getWriter().write(json);
    }

    private String safeMsg(String msg) {
        if (msg == null) return "无权限";
        return msg.replace('"','\'');
    }
}
