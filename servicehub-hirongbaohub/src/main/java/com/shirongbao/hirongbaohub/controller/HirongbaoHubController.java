/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站公开访问接口，无需登录凭证
 */
package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.ProfileResponse;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hirongbaohub")
public class HirongbaoHubController {
    private final SiteProfileService siteProfileService;

    // 初始化个人网站公开接口
    public HirongbaoHubController(SiteProfileService siteProfileService) {
        this.siteProfileService = siteProfileService;
    }

    // 查询站点资料、社交名片与统计数字
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.success(siteProfileService.getProfile());
    }
}
