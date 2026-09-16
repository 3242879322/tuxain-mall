package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录成功后返回给前端的用户信息（含 JWT 令牌）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    /** 用户 ID */
    private Long id;

    /** 账号 */
    private String account;

    /** JWT 令牌（前端保存后每次请求携带在 Authorization 头） */
    private String token;

    /** 头像 URL */
    private String avatar;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String mobile;
}