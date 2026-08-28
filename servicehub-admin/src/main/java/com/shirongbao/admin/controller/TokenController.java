/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 管理员 Token 管理接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.authhub.dto.TokenCreateRequest;
import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.admin.dto.TokenStatusRequest;
import com.shirongbao.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {
    private final ServiceTokenService service;

    // 初始化 Token 管理服务
    public TokenController(ServiceTokenService service) {
        this.service = service;
    }

    // 查询服务 Token 列表
    @GetMapping
    public ApiResponse<List<ServiceToken>> list() {
        return ApiResponse.success(service.list());
    }

    // 创建服务 Token
    @PostMapping
    public ApiResponse<ServiceToken> create(@Valid @RequestBody TokenCreateRequest request) {
        return ApiResponse.success(service.create(request));
    }

    // 更新服务 Token 状态
    @PostMapping("/{id}/status")
    public ApiResponse<ServiceToken> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody TokenStatusRequest request) {
        return ApiResponse.success(service.updateStatus(id, request.status()));
    }

    // 删除服务 Token
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
