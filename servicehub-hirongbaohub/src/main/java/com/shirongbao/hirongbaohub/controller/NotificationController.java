package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteNotification;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SiteNotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SiteNotificationService service;
    public NotificationController(SiteNotificationService service) { this.service = service; }
    
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.success(0L);
        return ApiResponse.success(service.getUnreadCount(userId));
    }
    
    @GetMapping("/list")
    public ApiResponse<List<SiteNotification>> getList() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.success(List.of());
        return ApiResponse.success(service.getUserNotifications(userId, 50));
    }
    
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            service.markAllAsRead(userId);
        }
        return ApiResponse.success(null);
    }
}
