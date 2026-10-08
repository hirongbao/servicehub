/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 管理员认证接口
 */
package com.shirongbao.admin.controller;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.admin.dto.AdminLoginRequest;
import com.shirongbao.admin.security.AdminCredentialService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import jakarta.validation.Valid;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminCredentialService credentials;
    private final SiteUserMapper siteUserMapper;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody AdminLoginRequest request) {
        SiteUser user = siteUserMapper.selectOne(new QueryWrapper<SiteUser>().eq("account_name", request.username()));
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        String inputHash = DigestUtils.md5DigestAsHex(request.password().getBytes(StandardCharsets.UTF_8));
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        return ApiResponse.success(Map.of("token", credentials.issue(request.username()), "username", request.username()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
