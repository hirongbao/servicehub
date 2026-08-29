/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: Token 调用记录查询接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usage")
public class UsageController {
    private final ServiceTokenService tokenService;

    // 初始化调用记录接口
    public UsageController(ServiceTokenService tokenService) {
        this.tokenService = tokenService;
    }

    // 分页查询 Token 调用记录，可按服务类型过滤
    @GetMapping
    public ApiResponse<Map<String, Object>> page(@RequestParam(defaultValue = "all") String hub,
                                                 @RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size) {
        String hubFilter = "all".equalsIgnoreCase(hub) ? null : hub.toUpperCase();
        return ApiResponse.success(tokenService.pageUsage(hubFilter, page, Math.min(Math.max(size, 1), 100)));
    }
}
