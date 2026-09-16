package com.tuxian.common.constant;

/**
 * Redis key 常量。
 */
public class RedisConstant {

    /** 购物车 Hash 的 key 前缀，完整 key = cart:{userId}，field = skuId，value = 购物车项 JSON */
    public static final String CART_KEY_PREFIX = "cart:";

    /** 用户端 Token 黑名单前缀，退出登录时写入，完整 key = user:token:blacklist:{token} */
    public static final String USER_TOKEN_BLACKLIST_PREFIX = "user:token:blacklist:";

    /** 管理端 Token 黑名单前缀 */
    public static final String ADMIN_TOKEN_BLACKLIST_PREFIX = "admin:token:blacklist:";

    /** 短信验证码 key 前缀，完整 key = login:code:{mobile} */
    public static final String LOGIN_CODE_PREFIX = "login:code:";
}