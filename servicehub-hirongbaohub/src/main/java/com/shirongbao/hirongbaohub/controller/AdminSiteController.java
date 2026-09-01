/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站内容管理接口（需管理员登录凭证）
 */
package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.dto.ProfileUpdateRequest;
import com.shirongbao.hirongbaohub.dto.SocialUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SiteProfile;
import com.shirongbao.hirongbaohub.entity.SiteSocial;
import com.shirongbao.hirongbaohub.service.SitePostService;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
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
@RequestMapping("/api/site")
public class AdminSiteController {
    private final SitePostService postService;
    private final SiteProfileService profileService;

    // 初始化站点内容管理接口
    public AdminSiteController(SitePostService postService, SiteProfileService profileService) {
        this.postService = postService;
        this.profileService = profileService;
    }

    // 查询动态列表
    @GetMapping("/posts")
    public ApiResponse<List<SitePost>> listPosts() {
        return ApiResponse.success(postService.list());
    }

    // 发布动态
    @PostMapping("/posts")
    public ApiResponse<SitePost> createPost(@Valid @RequestBody PostUpsertRequest request) {
        return ApiResponse.success(postService.create(request));
    }

    // 编辑动态
    @PostMapping("/posts/{id}")
    public ApiResponse<SitePost> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpsertRequest request) {
        return ApiResponse.success(postService.update(id, request));
    }

    // 更新动态状态（发布/下架）
    @PostMapping("/posts/{id}/status")
    public ApiResponse<SitePost> updatePostStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ApiResponse.success(postService.updateStatus(id, body.get("status")));
    }

    // 删除动态
    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.success();
    }

    // 查询站点资料与社媒名片
    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.success(Map.of(
                "profile", profileService.adminProfile(),
                "socials", profileService.adminSocials()));
    }

    // 更新站点资料
    @PostMapping("/profile")
    public ApiResponse<SiteProfile> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(profileService.updateProfile(request));
    }

    // 新增社媒名片
    @PostMapping("/socials")
    public ApiResponse<SiteSocial> createSocial(@Valid @RequestBody SocialUpsertRequest request) {
        return ApiResponse.success(profileService.createSocial(request));
    }

    // 更新社媒名片
    @PostMapping("/socials/{id}")
    public ApiResponse<SiteSocial> updateSocial(@PathVariable Long id, @Valid @RequestBody SocialUpsertRequest request) {
        return ApiResponse.success(profileService.updateSocial(id, request));
    }

    // 删除社媒名片
    @DeleteMapping("/socials/{id}")
    public ApiResponse<Void> deleteSocial(@PathVariable Long id) {
        profileService.deleteSocial(id);
        return ApiResponse.success();
    }
}
