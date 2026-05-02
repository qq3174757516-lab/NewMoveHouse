package com.newmovehouse.common;

public class UserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long id() {
        CurrentUser user = get();
        if (user == null) {
            throw new BizException("未登录");
        }
        return user.id;
    }

    public static String role() {
        CurrentUser user = get();
        if (user == null) {
            throw new BizException("未登录");
        }
        return user.role;
    }

    public static void clear() {
        HOLDER.remove();
    }
}

