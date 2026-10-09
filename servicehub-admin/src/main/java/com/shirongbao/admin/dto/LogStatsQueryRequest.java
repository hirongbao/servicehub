/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 访问日志统计概览查询参数对象
 */
package com.shirongbao.admin.dto;

import lombok.Data;

@Data
public class LogStatsQueryRequest {
    private String range = "24h";

    // 获取统计时间跨度小时数
    public int getIntervalHours() {
        return "7d".equalsIgnoreCase(range) ? 168 : 24;
    }
}
