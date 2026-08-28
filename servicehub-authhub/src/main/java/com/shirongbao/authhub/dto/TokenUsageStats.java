/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: Token 使用统计聚合结果
 */
package com.shirongbao.authhub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenUsageStats {
    private Long tokenId;
    private Long usageCount;
    private LocalDateTime lastUsedAt;
}
