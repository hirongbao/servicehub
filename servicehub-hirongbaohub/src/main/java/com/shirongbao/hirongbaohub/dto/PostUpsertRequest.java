/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 发布/编辑动态请求参数（内容与媒体至少其一，图片最多 9 张，视频 1 条）
 */
package com.shirongbao.hirongbaohub.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpsertRequest(@Size(max = 2000, message = "动态内容不能超过 2000 字") String content,
                                String mediaType,
                                List<@Size(max = 1024, message = "媒体链接过长") String> mediaUrls,
                                String categoryId,
                                @Size(max = 50, message = "分类名称过长") String categoryName) {
}
