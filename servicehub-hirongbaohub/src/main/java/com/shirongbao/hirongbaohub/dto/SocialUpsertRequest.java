/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 新增/更新社媒名片请求参数
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialUpsertRequest(@NotBlank(message = "平台名称不能为空") @Size(max = 50) String platform,
                                  @NotBlank(message = "图标名不能为空") @Size(max = 50) String iconName,
                                  @Size(max = 1024) String url,
                                  @Size(max = 1024) String qrCodeUrl,
                                  Integer sortOrder,
                                  Integer status) {
}
