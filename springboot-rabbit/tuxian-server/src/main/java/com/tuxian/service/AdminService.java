package com.tuxian.service;

import com.tuxian.pojo.dto.AdminLoginDTO;

import java.util.Map;

public interface AdminService {

    /**
     * 管理员登录，返回管理端 JWT 令牌。
     */
    String login(AdminLoginDTO adminLoginDTO);

    /**
     * 数据统计看板（订单/用户/商品数量）。
     */
    Map<String, Long> dashboard();
}