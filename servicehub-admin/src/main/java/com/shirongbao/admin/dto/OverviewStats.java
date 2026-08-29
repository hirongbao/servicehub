/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 概览聚合统计响应
 */
package com.shirongbao.admin.dto;

import java.util.List;

public record OverviewStats(long activeTokens, long totalTokens,
                            long activeLinks, long totalLinks,
                            long totalFiles, List<RecentToken> recentTokens) {
}
