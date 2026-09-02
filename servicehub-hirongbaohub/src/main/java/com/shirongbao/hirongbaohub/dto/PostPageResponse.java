/*
 * auth: hirongbao
 * create: 2026-09-02
 * desc: 公开动态分页响应
 */
package com.shirongbao.hirongbaohub.dto;

import com.shirongbao.hirongbaohub.entity.SitePost;

import java.util.List;

public record PostPageResponse(List<SitePost> items, int page, int size, long total, boolean hasMore) {
}
