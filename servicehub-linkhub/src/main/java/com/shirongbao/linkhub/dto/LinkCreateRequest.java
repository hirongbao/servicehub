/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 创建短链请求参数
 */
package com.shirongbao.linkhub.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkCreateRequest(
        @NotBlank(message = "目标链接不能为空") String targetUrl,
        String code,
        String remark,
        Integer validDays) {
}
