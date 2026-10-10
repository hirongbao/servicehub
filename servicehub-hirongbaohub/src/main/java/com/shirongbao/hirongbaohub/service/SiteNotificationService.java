/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内通知业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.entity.SiteNotification;
import com.shirongbao.hirongbaohub.mapper.SiteNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteNotificationService {
    private final SiteNotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    // 发送站内通知并向客户端推送实时通知事件
    public void notify(Long userId, String type, Long sourceId, String sourceAuthor, String content) {
        if (userId == null) return;
        SiteNotification n = new SiteNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setSourceId(sourceId);
        n.setSourceAuthor(sourceAuthor);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        mapper.insert(n);

        long unreadCount = mapper.selectCount(new LambdaQueryWrapper<SiteNotification>().eq(SiteNotification::getUserId, userId).eq(SiteNotification::getIsRead, false));
        messagingTemplate.convertAndSend("/topic/notify/" + userId, Map.of(
                "type", type,
                "unreadNotificationCount", unreadCount,
                "unreadCount", unreadCount,
                "newNotification", n
        ));
    }

    // 查询用户最近通知列表
    public List<SiteNotification> getUserNotifications(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SiteNotification>()
                .eq(SiteNotification::getUserId, userId)
                .orderByDesc(SiteNotification::getCreatedAt)
                .last("LIMIT " + limit));
    }

    // 将用户的所有未读通知标记为已读
    public void markAllAsRead(Long userId) {
        SiteNotification n = new SiteNotification();
        n.setIsRead(true);
        mapper.update(n, new LambdaQueryWrapper<SiteNotification>().eq(SiteNotification::getUserId, userId).eq(SiteNotification::getIsRead, false));
        messagingTemplate.convertAndSend("/topic/notify/" + userId, Map.of(
                "type", "NOTIFICATION_READ",
                "unreadNotificationCount", 0L,
                "unreadCount", 0L
        ));
    }

    // 查询用户当前未读通知总数
    public long getUnreadCount(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<SiteNotification>().eq(SiteNotification::getUserId, userId).eq(SiteNotification::getIsRead, false));
    }
}
