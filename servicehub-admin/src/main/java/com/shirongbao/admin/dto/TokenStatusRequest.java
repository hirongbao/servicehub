/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 更新 Token 状态请求参数
 */
package com.shirongbao.admin.dto;

import jakarta.validation.constraints.NotNull;

public record TokenStatusRequest(@NotNull Integer status) {
}
