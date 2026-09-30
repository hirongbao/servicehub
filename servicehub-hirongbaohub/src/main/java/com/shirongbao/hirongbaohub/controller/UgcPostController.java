package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SitePostService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/ugc")
public class UgcPostController {

    private final SitePostService postService;
    private final SitePostMapper postMapper;
    private final SiteUserMapper userMapper;

    public UgcPostController(SitePostService postService, SitePostMapper postMapper, SiteUserMapper userMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @PostMapping("/add")
    public ApiResponse<SitePost> addPost(@RequestBody PostUpsertRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能发布动态");
        }
        
        // Use existing create logic but intercept it to set user_id and audit_status
        SitePost post = postService.create(request); // This currently sets status=1 and doesn't set user_id.
        post.setUserId(userId);
        post.setAuditStatus(1); // 1 = approved
        post.setStatus(1); // Published
        postMapper.updateById(post); // update the fields set by service
        return ApiResponse.success(post);
    }
    
    // 用户的动态列表 (通过用户名查询)
    @GetMapping("/user/{accountName}")
    public ApiResponse<Page<SitePost>> getUserPosts(
            @PathVariable String accountName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        SiteUser targetUser = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        Page<SitePost> pg = new Page<>(page, size);
        postMapper.selectPage(pg, new LambdaQueryWrapper<SitePost>()
                .eq(SitePost::getUserId, targetUser.getId())
                .eq(SitePost::getStatus, 1)
                .orderByDesc(SitePost::getCreatedAt));
                
        postService.fillMedia(pg.getRecords());
        
        return ApiResponse.success(pg);
    }
}
