package com.tuxian.service;

import com.tuxian.pojo.dto.UserLoginDTO;
import com.tuxian.pojo.entity.User;
import com.tuxian.pojo.vo.UserLoginVO;

public interface UserService {

    /**
     * 用户登录（账号 + 密码）。
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 退出登录，把 token 加入 Redis 黑名单。
     */
    void logout(String token);

    /**
     * 获取当前登录用户信息。
     */
    User getCurrentUser();
}