/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 更新站点资料请求参数（统计数字未来由接口统计生成，不开放编辑）
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(@NotBlank(message = "名称不能为空") @Size(max = 50) String name,
                                   @NotBlank(message = "handle 不能为空") @Size(max = 50) String handle,
                                   @Size(max = 500) String bio,
                                   @Size(max = 1024) String avatarUrl) {
}
