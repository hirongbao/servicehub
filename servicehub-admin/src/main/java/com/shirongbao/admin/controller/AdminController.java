/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 管理员认证接口
 */
package com.shirongbao.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shirongbao.admin.dto.AdminLoginRequest;
import com.shirongbao.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final String adminUsername;
    private final String adminPassword;

    // 初始化管理员认证配置
    public AdminController(@Value("${servicehub.admin.username}") String adminUsername,
                           @Value("${servicehub.admin.password}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    // 校验管理员账号并签发登录 Token
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody AdminLoginRequest request) {
        if (!adminUsername.equals(request.username()) || !adminPassword.equals(request.password())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        StpUtil.login(request.username());
        return ApiResponse.success(Map.of("token", StpUtil.getTokenValue(), "username", request.username()));
    }

    // 注销当前管理员会话
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.success();
    }
}
