package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuxian.common.constant.JwtClaimsConstant;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.constant.RedisConstant;
import com.tuxian.common.context.BaseContext;
import com.tuxian.common.exception.BaseException;
import com.tuxian.common.properties.JwtProperties;
import com.tuxian.common.utils.JwtUtil;
import com.tuxian.mapper.UserMapper;
import com.tuxian.pojo.dto.UserLoginDTO;
import com.tuxian.pojo.entity.User;
import com.tuxian.pojo.vo.UserLoginVO;
import com.tuxian.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现。
 * <p>
 * 密码采用 BCrypt 加密：相比 MD5，BCrypt 自带随机盐，且不可逆、无法用彩虹表破解，
 * 是业界通行做法（MD5 已可被快速碰撞，不适合存密码）。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 1. 根据账号查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, userLoginDTO.getAccount()));
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验密码（BCrypt）
        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        // 3. 生成 JWT，载荷存放 userId
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 4. 组装返回
        return UserLoginVO.builder()
                .id(user.getId())
                .account(user.getAccount())
                .token(token)
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .mobile(user.getMobile())
                .build();
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            // 黑名单有效期 = token 剩余有效期，过期后自动删除，避免长期占用内存
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                stringRedisTemplate.opsForValue().set(
                        RedisConstant.USER_TOKEN_BLACKLIST_PREFIX + token, "1", remaining, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 令牌已失效，无需处理
        }
    }

    @Override
    public User getCurrentUser() {
        Long userId = BaseContext.getCurrentId();
        return userMapper.selectById(userId);
    }
}