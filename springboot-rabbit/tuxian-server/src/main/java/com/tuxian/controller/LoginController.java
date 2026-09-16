package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.UserLoginDTO;
import com.tuxian.pojo.entity.User;
import com.tuxian.pojo.vo.UserLoginVO;
import com.tuxian.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户登录相关接口。
 */
@Slf4j
@RestController
@Api(tags = "用户登录相关接口")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录（账号 + 密码）。
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody @Validated UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getAccount());
        return Result.success(userService.login(userLoginDTO));
    }

    /**
     * 退出登录：把 token 加入 Redis 黑名单。
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        userService.logout(extractToken(authorization));
        return Result.success();
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/member/user")
    @ApiOperation("获取当前用户信息")
    public Result<User> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 从 Authorization 头中解析出 token（去掉 Bearer 前缀）。
     */
    private String extractToken(String authorization) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
