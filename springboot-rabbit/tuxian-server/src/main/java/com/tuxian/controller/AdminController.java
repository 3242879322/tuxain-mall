package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.AdminLoginDTO;
import com.tuxian.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台管理接口（管理端使用独立 JWT，拦截 /admin/**）。
 * <p>
 * 目前前端未提供管理端界面，此模块为基础实现，具体管理接口待前端确认后补充。
 */
@RestController
@RequestMapping("/admin")
@Api(tags = "后台管理接口")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<String> login(@RequestBody @Validated AdminLoginDTO adminLoginDTO) {
        return Result.success(adminService.login(adminLoginDTO));
    }

    @GetMapping("/dashboard")
    @ApiOperation("数据统计看板")
    public Result<Map<String, Long>> dashboard() {
        return Result.success(adminService.dashboard());
    }
}
