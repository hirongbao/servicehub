/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: Token 调用记录条目
 */
package com.shirongbao.authhub.dto;

import java.time.LocalDateTime;

public record UsageLogItem(Long id, String tokenName, String hub, String action, LocalDateTime createdAt) {
}
