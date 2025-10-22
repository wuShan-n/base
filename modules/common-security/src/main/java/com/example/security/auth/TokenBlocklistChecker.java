package com.example.security.auth;

/**
 * 令牌黑名单检查 SPI。
 * 由上层应用实现并注入为 Bean，用于在请求期间拒绝被封禁/登出的 Access Token（基于 JTI）。
 */
public interface TokenBlocklistChecker {
    /**
     * @param jti JWT 的唯一标识（ID）
     * @return true 表示该 JTI 被拉黑，应拒绝访问
     */
    boolean isBlocked(String jti);
}
