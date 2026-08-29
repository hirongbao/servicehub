/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 面向 LINKHUB Token 的开放短链接口
 */
package com.shirongbao.linkhub.controller;

import com.shirongbao.authhub.entity.ServiceToken;
import jakarta.servlet.http.HttpServletRequest;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.linkhub.dto.LinkCreateRequest;
import com.shirongbao.linkhub.entity.ShortLink;
import com.shirongbao.linkhub.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/linkhub")
public class PublicLinkController {
    private static final String HUB = "LINKHUB";
    private final ShortLinkService service;
    private final ServiceTokenService tokenService;

    // 初始化开放短链接口
    public PublicLinkController(ShortLinkService service, ServiceTokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    // 使用 LINKHUB Token 创建短链
    @PostMapping("/links")
    public ApiResponse<ShortLink> create(@Valid @RequestBody LinkCreateRequest request,
                                         HttpServletRequest httpRequest,
                                         @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(httpRequest, serviceToken, authorization, "create");
        ShortLink link = service.create(request);
        link.setTargetUrl(service.fullUrl(link.getCode(), httpRequest));
        return ApiResponse.success(link);
    }

    // 使用 LINKHUB Token 查询短链详情和统计
    @GetMapping("/links/{code}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String code,
                                                   HttpServletRequest request,
                                                   @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(request, serviceToken, authorization, "query");
        ShortLink link = service.resolve(code);
        if (link == null) {
            throw new IllegalArgumentException("短链不存在、已禁用或已过期");
        }
        return ApiResponse.success(Map.of("link", link, "stats", service.stats(link.getId())));
    }

    // 校验服务 Token 并记录使用日志
    private void recordUsage(HttpServletRequest request, String serviceToken, String authorization, String action) {
        String token = serviceToken;
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7).trim();
        }
        ServiceToken serviceTokenEntity = tokenService.requireActive(token, HUB);
        request.setAttribute("auth.tokenName", serviceTokenEntity.getTokenName());
        tokenService.recordUsage(serviceTokenEntity, action);
    }
}
