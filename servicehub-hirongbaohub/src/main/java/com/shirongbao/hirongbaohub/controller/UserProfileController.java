package com.shirongbao.hirongbaohub.controller;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
import com.shirongbao.hirongbaohub.dto.ProfileResponse;
import com.shirongbao.hirongbaohub.dto.ProfileUpdateRequest;
import com.shirongbao.hirongbaohub.dto.SocialUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SiteProfile;
import com.shirongbao.hirongbaohub.entity.SiteSocial;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/profile/user")
@RequiredArgsConstructor
public class UserProfileController {
    private final SiteUserMapper userMapper;
    private final SiteProfileService siteProfileService;

    @GetMapping("/{accountName}")
    public ApiResponse<Object> getUserProfile(@PathVariable String accountName) {
        SiteUser user = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        // 如果是管理员账号（站点拥有者），直接返回站点配置的公开资料
        if ("ADMIN".equals(user.getRole())) {
            return ApiResponse.success(siteProfileService.getProfile());
        }
        
        // Parse socialLinks JSON if exists, else return empty list
        List<Map<String, String>> socials = new ArrayList<>();
        if (user.getSocialLinks() != null && !user.getSocialLinks().isBlank()) {
            try {
                // Using Jackson ObjectMapper to parse
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                socials = mapper.readValue(user.getSocialLinks(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                // ignore parsing error
            }
        }
        
        long postsCount = siteProfileService.getPostCountByUserId(user.getId());
        // Currently we don't have a real follower system, so mock a value based on user id or some heuristic.
        long followersCount = (user.getId() * 10) + 120; 
        
        return ApiResponse.success(Map.of(
            "name", user.getAccountName(),
            "handle", "@" + user.getAccountName(),
            "bio", user.getBio() != null && !user.getBio().isBlank() ? user.getBio() : "这个人很懒，什么都没写~",
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "stats", Map.of(
                "posts", postsCount, 
                "followers", followersCount,
                "following", 0
            ),
            "socials", socials
        ));
    }

    @PostMapping("/update")
    public ApiResponse<Object> updateUserProfile(@RequestBody Map<String, Object> request) {
        Long userId = com.shirongbao.hirongbaohub.security.UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("必须登录才能修改信息");
        }
        SiteUser user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        // Admin Profile Update
        if ("ADMIN".equals(user.getRole())) {
            SiteProfile adminProfile = siteProfileService.adminProfile();
            String avatarUrl = request.containsKey("avatarUrl") ? (String) request.get("avatarUrl") : adminProfile.getAvatarUrl();
            String bio = request.containsKey("bio") ? (String) request.get("bio") : adminProfile.getBio();
            
            ProfileUpdateRequest profileRequest = new ProfileUpdateRequest(
                adminProfile.getName(),
                adminProfile.getHandle(),
                bio,
                avatarUrl
            );
            siteProfileService.updateProfile(profileRequest);
            
            // Sync Admin's socials
            if (request.containsKey("socials")) {
                List<SiteSocial> existingSocials = siteProfileService.adminSocials();
                for (SiteSocial s : existingSocials) {
                    siteProfileService.deleteSocial(s.getId());
                }
                
                List<Map<String, String>> newSocials = (List<Map<String, String>>) request.get("socials");
                if (newSocials != null) {
                    for (int i = 0; i < newSocials.size(); i++) {
                        Map<String, String> sm = newSocials.get(i);
                        SocialUpsertRequest sur = new SocialUpsertRequest(
                            sm.get("platform"),
                            sm.get("iconName"),
                            sm.get("url"),
                            sm.get("qrCodeUrl"),
                            i,
                            1
                        );
                        siteProfileService.createSocial(sur);
                    }
                }
            }
            
            // 同时同步更新 site_user 表的冗余字段，以便全站（留言板、右上角头像等）能正常显示
            user.setAvatarUrl(avatarUrl);
            user.setBio(bio);
            userMapper.updateById(user);
            
            return ApiResponse.success(siteProfileService.getProfile());
        }
        
        // UGC Profile Update
        if (request.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) request.get("avatarUrl"));
        }
        if (request.containsKey("bio")) {
            user.setBio((String) request.get("bio"));
        }
        if (request.containsKey("socials")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String socialsStr = mapper.writeValueAsString(request.get("socials"));
                user.setSocialLinks(socialsStr);
            } catch (Exception e) {
                // ignore
            }
        }
        userMapper.updateById(user);
        return ApiResponse.success(user);
    }
}
