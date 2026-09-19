package com.shirongbao.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SitePasswordInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String expectedPassword = System.getenv("HIRONGBAO_PASSWORD");
        if (expectedPassword == null || expectedPassword.trim().isEmpty()) {
            return true;
        }
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String providedPassword = request.getHeader("X-Site-Password");
        if (expectedPassword.equals(providedPassword)) {
            return true;
        }

        response.setStatus(401);
        return false;
    }
}
