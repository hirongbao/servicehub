/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 创建 Token 请求参数
 */
package com.shirongbao.authhub.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenCreateRequest(@NotBlank(message = "Token 名称不能为空") String tokenName, String tokenType, Integer validDays, Integer maxUses) {
}
