/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 管理员登录请求参数
 */
package com.shirongbao.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(@NotBlank String username, @NotBlank String password) {
}
