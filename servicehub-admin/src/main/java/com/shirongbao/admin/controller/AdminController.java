/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 管理员认证接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.admin.dto.AdminLoginRequest;
import com.shirongbao.admin.security.AdminCredentialService;
import com.shirongbao.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final String adminUsername;
    private final String adminPassword;
    private final AdminCredentialService credentials;

    // 初始化管理员认证配置
    public AdminController(@Value("${servicehub.admin.username}") String adminUsername,
                           @Value("${servicehub.admin.password}") String adminPassword,
                           AdminCredentialService credentials) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.credentials = credentials;
    }

    // 校验管理员账号并签发登录凭证
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody AdminLoginRequest request) {
        if (!matches(adminUsername, request.username()) || !matches(adminPassword, request.password())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return ApiResponse.success(Map.of("token", credentials.issue(request.username()), "username", request.username()));
    }

    // 注销当前管理员会话
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }

    // 常数时间比较字符串，避免时序侧信道
    private boolean matches(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
