package com.tuxian.interceptor;

import com.tuxian.common.constant.JwtClaimsConstant;
import com.tuxian.common.constant.MessageConstant;
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
 * 用户端 JWT 拦截器。
 * <p>
 * 校验流程：
 * 1. 从请求头 Authorization 中取出 token（前端格式：Authorization: Bearer xxx）；
 * 2. 校验 token 是否在 Redis 黑名单（已退出登录）；
 * 3. 解析 token 得到 userId，存入 BaseContext；
 * 4. 校验失败返回 401。
 */
@Slf4j
@Component
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截 Controller 方法，放行静态资源等
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
        }

        // 2. 校验 token
        if (StringUtils.hasText(token)) {
            try {
                // 2.1 黑名单校验（已退出登录的 token 直接拒绝）
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisConstant.USER_TOKEN_BLACKLIST_PREFIX + token))) {
                    writeUnauthorized(response, MessageConstant.USER_NOT_LOGIN);
                    return false;
                }

                // 2.2 解析 token 获取 userId
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

                // 2.3 存入 ThreadLocal
                BaseContext.setCurrentId(userId);
                return true;
            } catch (Exception ex) {
                log.warn("用户端 token 校验失败：{}", ex.getMessage());
            }
        }

        writeUnauthorized(response, MessageConstant.USER_NOT_LOGIN);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 防止线程复用导致的内存泄漏
        BaseContext.removeCurrentId();
    }

    /**
     * 返回 401 与统一结构的 JSON。
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":\"0\",\"msg\":\"" + message + "\",\"message\":\"" + message + "\",\"result\":null}");
    }
}