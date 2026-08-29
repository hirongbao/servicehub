/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 概览聚合统计接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.admin.dto.OverviewStats;
import com.shirongbao.admin.dto.RecentToken;
import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.service.FileRecordService;
import com.shirongbao.linkhub.service.ShortLinkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/overview")
public class OverviewController {
    private final ServiceTokenService tokenService;
    private final ShortLinkService linkService;
    private final FileRecordService fileService;

    // 初始化概览聚合服务
    public OverviewController(ServiceTokenService tokenService, ShortLinkService linkService, FileRecordService fileService) {
        this.tokenService = tokenService;
        this.linkService = linkService;
        this.fileService = fileService;
    }

    // 查询概览聚合统计
    @GetMapping
    public ApiResponse<OverviewStats> overview() {
        List<RecentToken> recent = tokenService.recent(5).stream()
                .map(t -> new RecentToken(
                        t.getTokenName(),
                        t.getStatus() == 1 && (t.getExpiresAt() == null || t.getExpiresAt().isAfter(LocalDateTime.now())),
                        t.getExpiresAt(),
                        t.getCreatedAt()))
                .toList();
        OverviewStats stats = new OverviewStats(
                tokenService.countActive(), tokenService.countAll(),
                linkService.countActive(), linkService.countAll(),
                fileService.countAll(), recent);
        return ApiResponse.success(stats);
    }
}
