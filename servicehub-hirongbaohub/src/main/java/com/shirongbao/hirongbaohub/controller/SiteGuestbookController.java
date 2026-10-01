package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.service.SiteGuestbookService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/guestbook")
public class SiteGuestbookController {
    
    private final SiteGuestbookService guestbookService;
    private final SiteUserMapper userMapper;

    public SiteGuestbookController(SiteGuestbookService guestbookService, SiteUserMapper userMapper) {
        this.guestbookService = guestbookService;
        this.userMapper = userMapper;
    }

    private Long getAdminUserId() {
        SiteUser admin = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getRole, "ADMIN").last("LIMIT 1"));
        return admin != null ? admin.getId() : null;
    }

    public record GuestbookRequest(String content) {}

    @PostMapping("/add")
    public ApiResponse<Void> addMessage(@RequestBody GuestbookRequest req) {
        guestbookService.addMessage(req.content(), getAdminUserId());
        return ApiResponse.success();
    }

    @PostMapping("/add/user/{accountName}")
    public ApiResponse<Void> addUserMessage(@PathVariable String accountName, @RequestBody GuestbookRequest req) {
        SiteUser user = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        guestbookService.addMessage(req.content(), user.getId());
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(guestbookService.getMessageList(page, size, getAdminUserId()));
    }

    @GetMapping("/list/user/{accountName}")
    public ApiResponse<Page<Map<String, Object>>> userList(@PathVariable String accountName,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        SiteUser user = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(guestbookService.getMessageList(page, size, user.getId()));
    }
}
