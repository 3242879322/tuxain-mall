package com.tuxian.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类，读取 application.yml 中 tuxian.jwt 下的配置。
 * <p>
 * 用户端与管理端使用两套独立密钥与 Token 名称，保证两端认证互不干扰（对标苍穹外卖）。
 */
@Component
@ConfigurationProperties(prefix = "tuxian.jwt")
@Data
public class JwtProperties {

    /** 用户端签名密钥 */
    private String userSecretKey;
    /** 用户端令牌有效期（毫秒） */
    private long userTtl;
    /** 用户端令牌在请求头中的名称（前端实际通过 Authorization: Bearer xxx 传递） */
    private String userTokenName;

    /** 管理端签名密钥 */
    private String adminSecretKey;
    /** 管理端令牌有效期（毫秒） */
    private long adminTtl;
    /** 管理端令牌在请求头中的名称 */
    private String adminTokenName;
}