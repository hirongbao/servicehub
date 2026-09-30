package com.shirongbao.hirongbaohub.security;

public class UserContext {
    private static final ThreadLocal<UserCredentialService.Parsed> CONTEXT = new ThreadLocal<>();

    public static void set(UserCredentialService.Parsed user) {
        CONTEXT.set(user);
    }

    public static UserCredentialService.Parsed get() {
        return CONTEXT.get();
    }
    
    public static Long getUserId() {
        UserCredentialService.Parsed p = get();
        return p != null ? p.userId() : null;
    }
    
    public static boolean isAdmin() {
        UserCredentialService.Parsed p = get();
        return p != null && "ADMIN".equals(p.role());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
