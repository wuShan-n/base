package com.example.base.security.tenant;

import com.example.base.security.core.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenantHeader = request.getHeader(SecurityConstants.TENANT_HEADER);
            Long tenantId = 0L;
            if (tenantHeader != null && !tenantHeader.isBlank()) {
                try {
                    tenantId = Long.parseLong(tenantHeader.trim());
                } catch (NumberFormatException ignored) {
                    tenantId = 0L;
                }
            }
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
