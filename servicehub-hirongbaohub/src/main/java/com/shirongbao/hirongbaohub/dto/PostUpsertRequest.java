/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 发布/编辑动态请求参数
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpsertRequest(@NotBlank(message = "动态内容不能为空")
                                @Size(max = 2000, message = "动态内容不能超过 2000 字") String content,
                                String mediaType,
                                String mediaUrl) {
}
