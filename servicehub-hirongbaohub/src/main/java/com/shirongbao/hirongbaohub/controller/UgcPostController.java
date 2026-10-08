package com.shirongbao.hirongbaohub.controller;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SitePostService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/ugc")
@RequiredArgsConstructor
public class UgcPostController {

    private final SitePostService postService;
    private final SiteUserMapper userMapper;

    @PostMapping("/add")
    public ApiResponse<SitePost> addPost(@RequestBody PostUpsertRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能发布动态");
        }
        
        SitePost post = postService.createUgcPost(request, userId);
        return ApiResponse.success(post);
    }
    
    @GetMapping("/user/{accountName}")
    public ApiResponse<com.shirongbao.hirongbaohub.dto.PostPageResponse> getUserPosts(
            @PathVariable String accountName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        SiteUser targetUser = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        return ApiResponse.success(postService.userPage(targetUser.getId(), page, size));
    }
}
