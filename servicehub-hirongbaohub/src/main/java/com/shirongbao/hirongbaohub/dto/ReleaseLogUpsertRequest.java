/*
 * auth: hirongbao
 * create: 2026-09-03
 * desc: 更新日志新增与编辑请求参数
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;

public record ReleaseLogUpsertRequest(@NotBlank(message = "更新标题不能为空") String title,
                                      String version, String summary, String content) {
}
