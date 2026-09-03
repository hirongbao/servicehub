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
import com.shirongbao.hirongbaohub.dto.PostPageResponse;
import com.shirongbao.hirongbaohub.entity.SiteComment;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SiteReleaseLog;
import com.shirongbao.hirongbaohub.service.SitePostService;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
import com.shirongbao.hirongbaohub.service.SiteReleaseLogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hirongbaohub")
public class HirongbaoHubController {
    private final SiteProfileService siteProfileService;
    private final SitePostService sitePostService;
    private final SiteReleaseLogService releaseLogService;

    // 初始化个人网站公开接口
    public HirongbaoHubController(SiteProfileService siteProfileService, SitePostService sitePostService, SiteReleaseLogService releaseLogService) {
        this.siteProfileService = siteProfileService;
        this.sitePostService = sitePostService;
        this.releaseLogService = releaseLogService;
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

    // 分页查询已发布动态
    @GetMapping("/posts/page")
    public ApiResponse<PostPageResponse> postPage(@RequestParam(required = false) String category,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.success(sitePostService.publishedPage(category, page, size));
    }

    // 查询已发布更新日志
    @GetMapping("/releases")
    public ApiResponse<List<SiteReleaseLog>> releases() { return ApiResponse.success(releaseLogService.published()); }

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
    public ApiResponse<Map<String, Object>> heartbeat(@Valid @RequestBody HeartbeatRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(sitePostService.heartbeat(request.clientId(), resolveClientIp(httpRequest)));
    }

    // 读取可信反向代理后的真实客户端地址
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = normalizeIp(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddr)) return remoteAddr;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String candidate = normalizeIp(forwarded.split(",", 2)[0]);
            if (isValidClientIp(candidate)) return candidate;
        }
        String real = normalizeIp(request.getHeader("X-Real-IP"));
        return isValidClientIp(real) ? real : remoteAddr;
    }

    // 判断请求是否来自本机反向代理
    private boolean isTrustedProxy(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    // 规范化 IPv4 映射的回环地址
    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) return "";
        String value = ip.trim();
        return value.startsWith("::ffff:") ? value.substring(7) : value;
    }

    // 校验代理头中的客户端地址格式
    private boolean isValidClientIp(String ip) {
        return !ip.isBlank() && ip.length() <= 45 && !ip.contains("/") && !ip.contains(" ");
    }

    // 心跳请求参数
    public record HeartbeatRequest(@jakarta.validation.constraints.NotBlank(message = "clientId 不能为空") String clientId) {
    }
}
