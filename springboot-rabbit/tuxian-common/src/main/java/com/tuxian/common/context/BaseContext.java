package com.tuxian.common.context;

/**
 * 基于 ThreadLocal 的线程上下文对象。
 * <p>
 * 拦截器在解析 JWT 之后，把当前登录用户 ID 存入本类；Service 层通过
 * {@link #getCurrentId()} 直接获取，无需在 Controller / Service 之间层层传递。
 * <p>
 * 注意：每个请求处理完成后，必须在拦截器 afterCompletion 中调用
 * {@link #removeCurrentId()} 清理，防止线程复用导致的内存泄漏。
 */
public class BaseContext {

    /** 当前登录用户 ID（用户端为 userId，管理端为 adminId） */
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}