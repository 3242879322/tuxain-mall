package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuxian.common.constant.JwtClaimsConstant;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.exception.BaseException;
import com.tuxian.common.properties.JwtProperties;
import com.tuxian.common.utils.JwtUtil;
import com.tuxian.mapper.AdminMapper;
import com.tuxian.mapper.GoodsMapper;
import com.tuxian.mapper.OrdersMapper;
import com.tuxian.mapper.UserMapper;
import com.tuxian.pojo.dto.AdminLoginDTO;
import com.tuxian.pojo.entity.Admin;
import com.tuxian.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(AdminLoginDTO adminLoginDTO) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, adminLoginDTO.getUsername()));
        if (admin == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(adminLoginDTO.getPassword(), admin.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        return JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
    }

    @Override
    public Map<String, Long> dashboard() {
        Map<String, Long> map = new HashMap<>();
        map.put("orderCount", ordersMapper.selectCount(null));
        map.put("userCount", userMapper.selectCount(null));
        map.put("goodsCount", goodsMapper.selectCount(null));
        return map;
    }
}