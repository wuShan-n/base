package com.example.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个请求注入 traceId：优先使用 X-Request-Id / X-Trace-Id，否则生成。
 * 并回传到响应头，便于前后端关联日志。
 */
public class TraceIdFilter implements Filter {
    public static final String TRACE_ID = "traceId";
    public static final String HDR_REQUEST_ID = "X-Request-Id";
    public static final String HDR_TRACE_ID = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String traceId = req.getHeader(HDR_REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = req.getHeader(HDR_TRACE_ID);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(TRACE_ID, traceId);
        resp.setHeader(HDR_TRACE_ID, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
