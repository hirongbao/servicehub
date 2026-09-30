package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profile/user")
public class UserProfileController {
    private final SiteUserMapper userMapper;

    public UserProfileController(SiteUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/{accountName}")
    public ApiResponse<Map<String, Object>> getUserProfile(@PathVariable String accountName) {
        SiteUser user = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        // Parse socialLinks JSON if exists, else return empty list
        java.util.List<Map<String, String>> socials = new java.util.ArrayList<>();
        if (user.getSocialLinks() != null && !user.getSocialLinks().isBlank()) {
            try {
                // Using Jackson ObjectMapper to parse
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                socials = mapper.readValue(user.getSocialLinks(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Map<String, String>>>() {});
            } catch (Exception e) {
                // ignore parsing error
            }
        }
        
        return ApiResponse.success(Map.of(
            "name", user.getAccountName(),
            "handle", "@" + user.getAccountName(),
            "bio", user.getBio() != null && !user.getBio().isBlank() ? user.getBio() : "这个人很懒，什么都没写~",
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "stats", Map.of(
                "posts", 0, // Mock, or could query COUNT(*)
                "followers", 0,
                "following", 0
            ),
            "socials", socials
        ));
    }

    @org.springframework.web.bind.annotation.PostMapping("/update")
    public ApiResponse<SiteUser> updateUserProfile(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> request) {
        Long userId = com.shirongbao.hirongbaohub.security.UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("必须登录才能修改信息");
        }
        SiteUser user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
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
