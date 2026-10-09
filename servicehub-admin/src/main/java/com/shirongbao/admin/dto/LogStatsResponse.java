/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 访问日志聚合统计响应对象
 */
package com.shirongbao.admin.dto;

import java.util.List;
import java.util.Map;

public record LogStatsResponse(
        Map<String, Object> summary,
        List<Map<String, Object>> hourlyTrend,
        List<Map<String, Object>> statusDistribution,
        List<Map<String, Object>> topPaths,
        List<Map<String, Object>> topIps,
        List<Map<String, Object>> latencyDistribution) {
}
