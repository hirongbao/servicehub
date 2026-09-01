/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 动态点赞请求参数
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;

public record LikeRequest(@NotBlank(message = "action 不能为空") String action) {
}
