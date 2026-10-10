/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内通知接口控制器
 */
package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteNotification;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SiteNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SiteNotificationService service;

    // 获取当前用户未读通知总数
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.success(0L);
        return ApiResponse.success(service.getUnreadCount(userId));
    }

    // 获取当前用户最近通知列表
    @GetMapping("/list")
    public ApiResponse<List<SiteNotification>> getList() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.success(List.of());
        return ApiResponse.success(service.getUserNotifications(userId, 50));
    }

    // 将当前用户所有通知标记为已读
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            service.markAllAsRead(userId);
        }
        return ApiResponse.success(null);
    }
}
