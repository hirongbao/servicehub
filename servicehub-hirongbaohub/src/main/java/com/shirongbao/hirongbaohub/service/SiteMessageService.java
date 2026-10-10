/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 站内私信业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.hirongbaohub.dto.MessageResponse;
import com.shirongbao.hirongbaohub.dto.MessageSessionResponse;
import com.shirongbao.hirongbaohub.entity.SiteMessage;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteMessageMapper;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteMessageService {

    private final SiteMessageMapper messageMapper;
    private final SiteUserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SiteNotificationService noticeService;

    // 发送私信并推送通知
    @Transactional(rollbackFor = Exception.class)
    public MessageResponse send(Long senderId, Long receiverId, String content) {
        SiteMessage message = new SiteMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        SiteUser sender = userMapper.selectById(senderId);
        MessageResponse dto = new MessageResponse();
        dto.setId(message.getId());
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setContent(content);
        dto.setIsRead(false);
        dto.setCreatedAt(message.getCreatedAt());
        if (sender != null) {
            String senderDisplayName = (sender.getNickname() != null && !sender.getNickname().isBlank()) ? sender.getNickname() : sender.getAccountName();
            dto.setSenderName(senderDisplayName);
            dto.setSenderAvatar(sender.getAvatarUrl());
        }

        // 推送实时私信消息与未读数给接收方
        long unreadCount = getUnreadCount(receiverId);
        Map<String, Object> payload = Map.of(
                "type", "MESSAGE",
                "unreadCount", unreadCount,
                "message", dto
        );
        messagingTemplate.convertAndSend("/topic/notify/" + receiverId, payload);

        // 写入通知中心记录
        String senderName = sender != null ? ((sender.getNickname() != null && !sender.getNickname().isBlank()) ? sender.getNickname() : sender.getAccountName()) : "用户";
        String summary = content.length() > 30 ? content.substring(0, 30) + "..." : content;
        noticeService.notify(receiverId, "MESSAGE", message.getId(), senderName, summary);

        return dto;
    }

    // 获取会话列表
    public List<MessageSessionResponse> getSessions(Long userId) {
        return messageMapper.getSessions(userId);
    }

    // 获取聊天历史记录
    @Transactional(rollbackFor = Exception.class)
    public Page<MessageResponse> getHistory(Long currentUserId, Long otherUserId, int page, int size) {
        // 更新未读状态为已读
        LambdaUpdateWrapper<SiteMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SiteMessage::getReceiverId, currentUserId)
                .eq(SiteMessage::getSenderId, otherUserId)
                .eq(SiteMessage::getIsRead, false)
                .set(SiteMessage::getIsRead, true);
        messageMapper.update(null, updateWrapper);

        // 获取更新后的总未读数并推送给当前用户
        notifyUnreadCount(currentUserId);

        // 分页查询历史消息
        Page<SiteMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<SiteMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.nested(i -> i.eq(SiteMessage::getSenderId, currentUserId).eq(SiteMessage::getReceiverId, otherUserId))
                .or(i -> i.eq(SiteMessage::getSenderId, otherUserId).eq(SiteMessage::getReceiverId, currentUserId))
                .orderByDesc(SiteMessage::getCreatedAt);

        messageMapper.selectPage(messagePage, queryWrapper);

        // 批量查询用户信息
        List<Long> userIds = messagePage.getRecords().stream()
                .map(SiteMessage::getSenderId)
                .distinct()
                .toList();
        Map<Long, SiteUser> userMap = userIds.isEmpty() ? java.util.Collections.emptyMap() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SiteUser::getId, u -> u));

        List<MessageResponse> dtos = messagePage.getRecords().stream().map(msg -> {
            MessageResponse dto = new MessageResponse();
            dto.setId(msg.getId());
            dto.setSenderId(msg.getSenderId());
            dto.setReceiverId(msg.getReceiverId());
            dto.setContent(msg.getContent());
            dto.setIsRead(msg.getIsRead());
            dto.setCreatedAt(msg.getCreatedAt());
            SiteUser sender = userMap.get(msg.getSenderId());
            if (sender != null) {
                String senderDisplayName = (sender.getNickname() != null && !sender.getNickname().isBlank()) ? sender.getNickname() : sender.getAccountName();
                dto.setSenderName(senderDisplayName);
                dto.setSenderAvatar(sender.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // 按时间升序（从旧到新）排列历史消息
        java.util.Collections.reverse(dtos);

        Page<MessageResponse> responsePage = new Page<>(page, size, messagePage.getTotal());
        responsePage.setRecords(dtos);
        return responsePage;
    }

    // 获取总未读数
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SiteMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SiteMessage::getReceiverId, userId)
                .eq(SiteMessage::getIsRead, false);
        return messageMapper.selectCount(queryWrapper);
    }

    // 发送 WebSocket 未读消息通知
    private void notifyUnreadCount(Long userId) {
        long unreadCount = getUnreadCount(userId);
        Map<String, Object> payload = Map.of(
                "type", "MESSAGE",
                "unreadCount", unreadCount
        );
        messagingTemplate.convertAndSend("/topic/notify/" + userId, payload);
    }
}
