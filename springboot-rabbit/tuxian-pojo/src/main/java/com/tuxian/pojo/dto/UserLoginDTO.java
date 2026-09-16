package com.tuxian.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求参数。
 */
@Data
public class UserLoginDTO implements Serializable {

    /** 账号（手机号） */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}