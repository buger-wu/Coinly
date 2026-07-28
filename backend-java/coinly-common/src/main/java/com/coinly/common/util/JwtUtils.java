package com.coinly.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 * 负责 Token 的生成、解析、校验，是鉴权体系的核心组件。
 * @see com.coinly.business.auth.interceptor.JwtInterceptor
 */
public class JwtUtils {

    /** JWT 签名密钥（第一版硬编码，生产环境应改为配置注入） */
    private static final String SECRET_KEY = "coinly-secret-key-for-jwt-token-generation-2024";

    /** Token 有效期：7 天（单位：毫秒） */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 构建签名密钥。
     * 使用 HMAC-SHA 算法，密钥长度需 ≥ 256 位（32 字节）。
     */
    private static SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token。
     *
     * @param userId 用户 ID，作为 Token 的 subject
     * @return 签名后的 JWT 字符串
     */
    public static String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中解析用户 ID。
     *
     * @param token JWT 字符串（不含 "Bearer " 前缀）
     * @return 用户 ID
     * @throws io.jsonwebtoken.JwtException Token 无效或已过期时抛出
     */
    public static Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 校验 Token 是否有效（未过期 + 签名正确）。
     *
     * @param token JWT 字符串
     * @return true=有效，false=无效或已过期
     */
    public static boolean isValidToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}