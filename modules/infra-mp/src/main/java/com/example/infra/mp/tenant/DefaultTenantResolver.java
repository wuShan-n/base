//package com.example.infra.mp.tenant;
//
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.Optional;
//
///**
// * 默认租户解析：优先从 SecurityContext 的 JWT claim "tenant"，
// * 否则读取请求头 X-Tenant-Id。
// */
//@Component
//public class DefaultTenantResolver implements TenantResolver {
//
//    public static final String HDR_TENANT_ID = "X-Tenant-Id";
//
//    @Override
//    public Optional<Long> resolveTenantId() {
//        // 1) 从 JWT 中读 "tenant"
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth instanceof JwtAuthenticationToken token) {
//            Jwt jwt = token.getToken();
//            Object claim = jwt.getClaim("tenant");
//            if (claim != null) {
//                try {
//                    return Optional.of(Long.parseLong(claim.toString()));
//                } catch (NumberFormatException ignored) {}
//            }
//        }
//        // 2) 从请求头读取
//        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
//        if (ra instanceof ServletRequestAttributes attrs) {
//            HttpServletRequest req = attrs.getRequest();
//            String hdr = req.getHeader(HDR_TENANT_ID);
//            if (hdr != null && !hdr.isBlank()) {
//                try {
//                    return Optional.of(Long.parseLong(hdr));
//                } catch (NumberFormatException ignored) {}
//            }
//        }
//        return Optional.empty();
//    }
//}
