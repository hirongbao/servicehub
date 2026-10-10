/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 私信会话响应DTO
 */
package com.shirongbao.hirongbaohub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageSessionResponse {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;

    // 兼容前端 target* 字段
    public Long getTargetUserId() {
        return otherUserId;
    }

    public String getTargetNickname() {
        return otherUserName;
    }

    public String getTargetAccountName() {
        return otherUserName;
    }

    public String getTargetAvatarUrl() {
        return otherUserAvatar;
    }

    public String getLastMessage() {
        return lastMessageContent;
    }
}
