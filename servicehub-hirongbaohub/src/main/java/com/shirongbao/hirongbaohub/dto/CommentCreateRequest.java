/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 访客评论请求参数
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(@Size(max = 50, message = "昵称不能超过 50 字") String author,
                                   @NotBlank(message = "评论内容不能为空")
                                   @Size(max = 500, message = "评论内容不能超过 500 字") String content) {
}
