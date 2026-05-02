package com.newmovehouse.common;

/**
 * 基于 {@link ThreadLocal} 的登录用户上下文，在拦截器入口写入、请求结束清理。
 */
public class UserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /** 绑定当前线程用户 */
    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /** 获取当前线程用户，可能为 null（未经过鉴权拦截器） */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /**
     * 当前用户主键，未登录时抛出 {@link BizException}。
     */
    public static Long id() {
        CurrentUser user = get();
        if (user == null) {
            throw new BizException("未登录");
        }
        return user.id;
    }

    /**
     * 当前用户角色，未登录时抛出 {@link BizException}。
     */
    public static String role() {
        CurrentUser user = get();
        if (user == null) {
            throw new BizException("未登录");
        }
        return user.role;
    }

    /** 请求结束后移除 ThreadLocal，避免线程池复用泄漏 */
    public static void clear() {
        HOLDER.remove();
    }
}
