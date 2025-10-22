package com.example.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 简单的请求日志（方法、路径、状态、耗时）。
 * 不打印请求体，避免性能与隐私问题。
 */
public class RequestLoggingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            int status = (response instanceof HttpServletResponse r) ? r.getStatus() : 200;
            long cost = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({} ms)", req.getMethod(), req.getRequestURI(), status, cost);
        }
    }
}
