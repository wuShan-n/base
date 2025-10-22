package com.example.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 401 统一输出
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = String.format(
                "{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
                safeMsg(authException.getMessage()),
                Instant.now().toString(),
                request.getRequestURI()
        );
        response.getWriter().write(json);
    }

    private String safeMsg(String msg) {
        if (msg == null) return "未认证";
        return msg.replace('"','\'');
    }
}
