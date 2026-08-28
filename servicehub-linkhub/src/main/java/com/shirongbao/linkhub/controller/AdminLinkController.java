/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链管理接口
 */
package com.shirongbao.linkhub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.linkhub.dto.LinkCreateRequest;
import com.shirongbao.linkhub.dto.LinkStatusRequest;
import com.shirongbao.linkhub.entity.ShortLink;
import com.shirongbao.linkhub.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/links")
public class AdminLinkController {
    private final ShortLinkService service;

    // 初始化短链管理服务
    public AdminLinkController(ShortLinkService service) {
        this.service = service;
    }

    // 查询短链列表
    @GetMapping
    public ApiResponse<List<ShortLink>> list() {
        return ApiResponse.success(service.list());
    }

    // 创建短链
    @PostMapping
    public ApiResponse<ShortLink> create(@Valid @RequestBody LinkCreateRequest request, HttpServletRequest httpRequest) {
        ShortLink link = service.create(request);
        link.setTargetUrl(service.fullUrl(link.getCode(), httpRequest));
        return ApiResponse.success(link);
    }

    // 更新短链状态
    @PostMapping("/{id}/status")
    public ApiResponse<ShortLink> updateStatus(@PathVariable Long id, @Valid @RequestBody LinkStatusRequest request) {
        return ApiResponse.success(service.updateStatus(id, request.status()));
    }

    // 删除短链
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }

    // 查询短链访问统计
    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> stats(@PathVariable Long id) {
        return ApiResponse.success(service.stats(id));
    }
}
