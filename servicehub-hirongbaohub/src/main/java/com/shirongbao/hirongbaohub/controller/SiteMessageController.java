/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内私信接口
 */
package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.MessageResponse;
import com.shirongbao.hirongbaohub.dto.MessageSendRequest;
import com.shirongbao.hirongbaohub.dto.MessageSessionResponse;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SiteMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hirongbaohub/messages")
@RequiredArgsConstructor
public class SiteMessageController {

    private final SiteMessageService siteMessageService;
    private final SiteUserMapper userMapper;

    // 发送私信
    @PostMapping
    public ApiResponse<MessageResponse> send(@RequestBody MessageSendRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        Long receiverId = request.getReceiverId();
        if (receiverId == null && request.getReceiverAccount() != null && !request.getReceiverAccount().isBlank()) {
            SiteUser receiver = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, request.getReceiverAccount().trim()));
            if (receiver != null) {
                receiverId = receiver.getId();
            }
        }
        if (receiverId == null) {
            return ApiResponse.error("未找到接收方用户");
        }
        MessageResponse response = siteMessageService.send(userId, receiverId, request.getContent());
        return ApiResponse.success(response);
    }

    // 获取会话列表
    @GetMapping("/sessions")
    public ApiResponse<List<MessageSessionResponse>> getSessions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        List<MessageSessionResponse> sessions = siteMessageService.getSessions(userId);
        return ApiResponse.success(sessions);
    }

    // 获取聊天历史记录
    @GetMapping("/history/{otherUser}")
    public ApiResponse<Page<MessageResponse>> getHistory(
            @PathVariable String otherUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        Long otherUserId;
        try {
            otherUserId = Long.parseLong(otherUser);
        } catch (NumberFormatException e) {
            SiteUser u = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, otherUser.trim()));
            if (u == null) {
                return ApiResponse.success(new Page<>(page, size, 0));
            }
            otherUserId = u.getId();
        }
        Page<MessageResponse> history = siteMessageService.getHistory(userId, otherUserId, page, size);
        return ApiResponse.success(history);
    }

    // 获取总未读数
    @GetMapping("/unread")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        long count = siteMessageService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }
}
