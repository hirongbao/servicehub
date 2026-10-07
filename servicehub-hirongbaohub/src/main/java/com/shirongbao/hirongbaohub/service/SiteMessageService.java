/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 绔欏唴绉佷俊鏈嶅姟
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

    // 鍙戦€佺淇?    @Transactional(rollbackFor = Exception.class)
    public void send(Long senderId, Long receiverId, String content) {
        SiteMessage message = new SiteMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        // 閫氱煡鎺ユ敹鏂?        notifyUnreadCount(receiverId);
    }

    // 鑾峰彇浼氳瘽鍒楄〃
    public List<MessageSessionResponse> getSessions(Long userId) {
        return messageMapper.getSessions(userId);
    }

    // 鑾峰彇鑱婂ぉ鍘嗗彶璁板綍
    @Transactional(rollbackFor = Exception.class)
    public Page<MessageResponse> getHistory(Long currentUserId, Long otherUserId, int page, int size) {
        // 鏇存柊鏈鐘舵€佷负宸茶
        LambdaUpdateWrapper<SiteMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SiteMessage::getReceiverId, currentUserId)
                .eq(SiteMessage::getSenderId, otherUserId)
                .eq(SiteMessage::getIsRead, false)
                .set(SiteMessage::getIsRead, true);
        messageMapper.update(null, updateWrapper);

        // 鑾峰彇鏇存柊鍚庣殑鎬绘湭璇绘暟骞舵帹閫佺粰褰撳墠鐢ㄦ埛
        notifyUnreadCount(currentUserId);

        // 鍒嗛〉鏌ヨ鍘嗗彶娑堟伅
        Page<SiteMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<SiteMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.nested(i -> i.eq(SiteMessage::getSenderId, currentUserId).eq(SiteMessage::getReceiverId, otherUserId))
                .or(i -> i.eq(SiteMessage::getSenderId, otherUserId).eq(SiteMessage::getReceiverId, currentUserId))
                .orderByDesc(SiteMessage::getCreatedAt);
        
        messageMapper.selectPage(messagePage, queryWrapper);

        // 杞崲 DTO
        Page<MessageResponse> responsePage = new Page<>(page, size, messagePage.getTotal());
        
        // 鎵归噺鏌ヨ鐢ㄦ埛淇℃伅
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
                dto.setSenderName(sender.getNickname());
                dto.setSenderAvatar(sender.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        responsePage.setRecords(dtos);
        return responsePage;
    }

    // 鑾峰彇鎬绘湭璇绘暟
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SiteMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SiteMessage::getReceiverId, userId)
                .eq(SiteMessage::getIsRead, false);
        return messageMapper.selectCount(queryWrapper);
    }

    // 鍙戦€?WebSocket 鏈娑堟伅閫氱煡
    private void notifyUnreadCount(Long userId) {
        long unreadCount = getUnreadCount(userId);
        Map<String, Object> payload = Map.of(
                "type", "MESSAGE",
                "unreadCount", unreadCount
        );
        messagingTemplate.convertAndSend("/topic/notify/" + userId, payload);
    }
}

