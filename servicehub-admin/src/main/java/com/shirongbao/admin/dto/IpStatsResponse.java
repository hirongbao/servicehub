/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 指定 IP 访问统计与追踪响应对象
 */
package com.shirongbao.admin.dto;

import java.util.List;
import java.util.Map;

public record IpStatsResponse(
        Map<String, Object> summary,
        String region,
        List<Map<String, Object>> statusDistribution,
        List<Map<String, Object>> topPaths,
        List<Map<String, Object>> timeDistribution) {
}
