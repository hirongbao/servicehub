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
        
        // Mocking the profile format so it works with the frontend
        return ApiResponse.success(Map.of(
            "name", user.getAccountName(),
            "handle", "@" + user.getAccountName(),
            "bio", "这个人很懒，什么都没写~",
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "stats", Map.of(
                "posts", 0, // Mock, or could query COUNT(*)
                "followers", 0,
                "following", 0
            ),
            "socials", java.util.List.of()
        ));
    }
}
