package com.tuxian.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类。
 * <p>
 * 使用 HS256 算法签名，生成/解析令牌（依赖 jjwt 0.9.1）。
 */
public class JwtUtil {

    /**
     * 生成 JWT 令牌。
     *
     * @param secretKey 签名密钥
     * @param ttlMillis 有效期（毫秒）
     * @param claims    自定义载荷（如 userId）
     * @return JWT 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8));

        return builder.compact();
    }

    /**
     * 解析并校验 JWT 令牌。
     *
     * @param secretKey 签名密钥（与生成时一致）
     * @param token     JWT 字符串
     * @return 解析后的 Claims；签名错误或过期会抛出异常
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}