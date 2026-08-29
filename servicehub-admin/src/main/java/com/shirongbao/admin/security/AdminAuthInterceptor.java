/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 管理端接口登录凭证校验拦截器
 */
package com.shirongbao.admin.security;

import com.shirongbao.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final AdminCredentialService credentials;
    private final ObjectMapper objectMapper;

    // 初始化凭证校验拦截器
    public AdminAuthInterceptor(AdminCredentialService credentials, ObjectMapper objectMapper) {
        this.credentials = credentials;
        this.objectMapper = objectMapper;
    }

    // 校验请求携带的登录凭证，通过时记录用户名，失败时返回 401 和统一响应
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String credential = request.getHeader("Authorization");
        if (credential != null && credential.startsWith("Bearer ")) {
            credential = credential.substring(7).trim();
        } else {
            String legacy = request.getHeader("satoken");
            credential = legacy == null ? credential : legacy.trim();
        }
        String username = credentials.resolveUsername(credential);
        if (username != null) {
            request.setAttribute("auth.user", username);
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error("登录状态已失效，请重新登录")));
        return false;
    }
}
