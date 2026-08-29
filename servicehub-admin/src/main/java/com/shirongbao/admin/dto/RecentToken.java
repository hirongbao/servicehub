/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 概览页最近凭证投影，不暴露 Token 明文
 */
package com.shirongbao.admin.dto;

import java.time.LocalDateTime;

public record RecentToken(String tokenName, boolean active, LocalDateTime expiresAt, LocalDateTime createdAt) {
}
