package com.example.base.common.logging;

public final class LoggingConstants {

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private LoggingConstants() {
    }
}
