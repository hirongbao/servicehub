/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 发送私信请求DTO
 */
package com.shirongbao.hirongbaohub.dto;

import lombok.Data;

@Data
public class MessageSendRequest {
    private Long receiverId;
    private String content;
}
