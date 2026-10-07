/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 私信响应DTO
 */
package com.shirongbao.hirongbaohub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String senderName;
    private String senderAvatar;
}
