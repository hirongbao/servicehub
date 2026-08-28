/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 更新短链状态请求参数
 */
package com.shirongbao.linkhub.dto;

import jakarta.validation.constraints.NotNull;

public record LinkStatusRequest(@NotNull(message = "status 不能为空") Integer status) {
}
