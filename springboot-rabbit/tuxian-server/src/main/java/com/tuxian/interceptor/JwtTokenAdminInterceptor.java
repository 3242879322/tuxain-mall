package com.tuxian.interceptor;

import com.tuxian.common.constant.JwtClaimsConstant;
import com.tuxian.common.constant.RedisConstant;
import com.tuxian.common.context.BaseContext;
import com.tuxian.common.properties.JwtProperties;
import com.tuxian.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理端 JWT 拦截器。
 * <p>
 * 与用户端共用 BaseContext（存 adminId），但使用独立的签名密钥与 Token 名称，
 * 保证管理端与用户端令牌互不通用（对标苍穹外卖）。
 */
@Slf4j
@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
        }

        if (StringUtils.hasText(token)) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisConstant.ADMIN_TOKEN_BLACKLIST_PREFIX + token))) {
                    writeUnauthorized(response, "管理员未登录");
                    return false;
                }

                Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
                Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
                BaseContext.setCurrentId(adminId);
                return true;
            } catch (Exception ex) {
                log.warn("管理端 token 校验失败：{}", ex.getMessage());
            }
        }

        writeUnauthorized(response, "管理员未登录");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":\"0\",\"msg\":\"" + message + "\",\"message\":\"" + message + "\",\"result\":null}");
    }
}