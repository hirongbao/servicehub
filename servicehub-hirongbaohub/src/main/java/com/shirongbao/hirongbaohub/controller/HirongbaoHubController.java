/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站公开访问接口，无需登录凭证
 */
package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.CommentCreateRequest;
import com.shirongbao.hirongbaohub.dto.LikeRequest;
import com.shirongbao.hirongbaohub.dto.ProfileResponse;
import com.shirongbao.hirongbaohub.entity.SiteComment;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.service.SitePostService;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hirongbaohub")
public class HirongbaoHubController {
    private final SiteProfileService siteProfileService;
    private final SitePostService sitePostService;

    // 初始化个人网站公开接口
    public HirongbaoHubController(SiteProfileService siteProfileService, SitePostService sitePostService) {
        this.siteProfileService = siteProfileService;
        this.sitePostService = sitePostService;
    }

    // 查询站点资料、社交名片与统计数字
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.success(siteProfileService.getProfile());
    }

    // 查询已发布动态（含媒体与评论）
    @GetMapping("/posts")
    public ApiResponse<List<SitePost>> posts(@RequestParam(required = false) String category) {
        return ApiResponse.success(sitePostService.publishedList(category));
    }

    // 点赞或取消点赞
    @PostMapping("/posts/{id}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable Long id, @Valid @RequestBody LikeRequest request) {
        if (!"like".equals(request.action()) && !"unlike".equals(request.action())) {
            throw new IllegalArgumentException("action 只支持 like 或 unlike");
        }
        return ApiResponse.success(Map.of("likes", sitePostService.like(id, request.action())));
    }

    // 发表访客评论
    @PostMapping("/posts/{id}/comments")
    public ApiResponse<SiteComment> comment(@PathVariable Long id, @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(sitePostService.addComment(id, request));
    }

    // 刷新访客心跳并返回当前在线人数
    @PostMapping("/heartbeat")
    public ApiResponse<Map<String, Object>> heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return ApiResponse.success(Map.of("onlineCount", sitePostService.heartbeat(request.clientId())));
    }

    // 心跳请求参数
    public record HeartbeatRequest(@jakarta.validation.constraints.NotBlank(message = "clientId 不能为空") String clientId) {
    }
}
