package com.example.base.common.logging;

import com.example.base.security.core.SecurityUtils;
import com.example.base.security.tenant.TenantContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class RequestContextLoggingFilter extends OncePerRequestFilter {

    private final ObjectProvider<Tracer> tracerProvider;

    public RequestContextLoggingFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        response.setHeader(LoggingConstants.REQUEST_ID_HEADER, requestId);

        putMdc(LoggingConstants.MDC_REQUEST_ID, requestId);
        putMdc(LoggingConstants.MDC_TENANT_ID, String.valueOf(TenantContext.getTenantId()));
        SecurityUtils.getCurrentUserId().map(String::valueOf)
                .ifPresent(id -> putMdc(LoggingConstants.MDC_USER_ID, id));
        currentTraceId().ifPresent(traceId -> putMdc(LoggingConstants.MDC_TRACE_ID, traceId));
        currentSpanId().ifPresent(spanId -> putMdc(LoggingConstants.MDC_SPAN_ID, spanId));

        try {
            filterChain.doFilter(request, response);
        } finally {
            clearMdc();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String headerValue = request.getHeader(LoggingConstants.REQUEST_ID_HEADER);
        if (StringUtils.hasText(headerValue)) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }

    private Optional<String> currentTraceId() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer == null) {
            return Optional.empty();
        }
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(currentSpan.context().traceId());
    }

    private Optional<String> currentSpanId() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer == null) {
            return Optional.empty();
        }
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(currentSpan.context().spanId());
    }

    private void putMdc(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }

    private void clearMdc() {
        MDC.remove(LoggingConstants.MDC_REQUEST_ID);
        MDC.remove(LoggingConstants.MDC_TENANT_ID);
        MDC.remove(LoggingConstants.MDC_USER_ID);
        MDC.remove(LoggingConstants.MDC_TRACE_ID);
        MDC.remove(LoggingConstants.MDC_SPAN_ID);
    }
}
