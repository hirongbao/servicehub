/*
 * auth: hirongbao
 * create: 2026-09-30
 * desc: 用户上下文工具类，基于 ThreadLocal 保存当前线程已认证的用户信息
 */
package com.shirongbao.hirongbaohub.security;

public class UserContext {
    private static final ThreadLocal<UserCredentialService.Parsed> CONTEXT = new ThreadLocal<>();

    // 设置当前线程用户信息
    public static void set(UserCredentialService.Parsed user) {
        CONTEXT.set(user);
    }

    // 获取当前线程用户信息
    public static UserCredentialService.Parsed get() {
        return CONTEXT.get();
    }
    
    // 获取当前登录用户 ID
    public static Long getUserId() {
        UserCredentialService.Parsed p = get();
        return p != null ? p.userId() : null;
    }
    
    // 判断当前登录用户是否为管理员
    public static boolean isAdmin() {
        UserCredentialService.Parsed p = get();
        return p != null && "ADMIN".equals(p.role());
    }

    // 清除当前线程用户信息
    public static void clear() {
        CONTEXT.remove();
    }
}
