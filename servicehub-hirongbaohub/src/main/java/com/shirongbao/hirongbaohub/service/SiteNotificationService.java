package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.entity.SiteNotification;
import com.shirongbao.hirongbaohub.mapper.SiteNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SiteNotificationService {
    private final SiteNotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    public SiteNotificationService(SiteNotificationMapper mapper, SimpMessagingTemplate messagingTemplate) {
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }

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
        messagingTemplate.convertAndSend("/topic/notify/" + userId, Map.of("unreadCount", unreadCount, "newNotification", n));
    }
    
    public List<SiteNotification> getUserNotifications(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SiteNotification>()
                .eq(SiteNotification::getUserId, userId)
                .orderByDesc(SiteNotification::getCreatedAt)
                .last("LIMIT " + limit));
    }
    
    public void markAllAsRead(Long userId) {
        SiteNotification n = new SiteNotification();
        n.setIsRead(true);
        mapper.update(n, new LambdaQueryWrapper<SiteNotification>().eq(SiteNotification::getUserId, userId).eq(SiteNotification::getIsRead, false));
        messagingTemplate.convertAndSend("/topic/notify/" + userId, Map.of("unreadCount", 0));
    }
    
    public long getUnreadCount(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<SiteNotification>().eq(SiteNotification::getUserId, userId).eq(SiteNotification::getIsRead, false));
    }
}
