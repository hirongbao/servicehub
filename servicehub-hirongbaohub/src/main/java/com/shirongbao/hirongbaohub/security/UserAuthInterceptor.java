/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 用户登录拦截器，校验 Bearer 凭证并设置用户上下文
 */
package com.shirongbao.hirongbaohub.security;

import lombok.RequiredArgsConstructor;

import com.shirongbao.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserAuthInterceptor implements HandlerInterceptor {
    private final UserCredentialService credentials;
    private final ObjectMapper objectMapper;

    // 前置拦截：提取并校验 Authorization Bearer 凭证，注入 UserContext
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            UserCredentialService.Parsed parsed = credentials.verifyAndParse(token);
            if (parsed != null) {
                UserContext.set(parsed);
                request.setAttribute("auth.userId", parsed.userId());
                return true;
            }
        }
        
        reject(response);
        return false;
    }

    // 请求结束清理 UserContext 避免线程池复用污染
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    // 未登录时统一返回 401 错误响应
    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error("请先登录")));
    }
}
