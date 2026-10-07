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
}
