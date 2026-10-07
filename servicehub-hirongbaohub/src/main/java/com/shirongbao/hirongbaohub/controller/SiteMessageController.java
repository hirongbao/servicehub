/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内私信接口
 */
package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.MessageResponse;
import com.shirongbao.hirongbaohub.dto.MessageSendRequest;
import com.shirongbao.hirongbaohub.dto.MessageSessionResponse;
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

    // 发送私信
    @PostMapping
    public ApiResponse<Void> send(@RequestBody MessageSendRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }
        siteMessageService.send(userId, request.getReceiverId(), request.getContent());
        return ApiResponse.success();
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

    // 获取聊天历史
    @GetMapping("/history/{otherUserId}")
    public ApiResponse<Page<MessageResponse>> getHistory(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
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
