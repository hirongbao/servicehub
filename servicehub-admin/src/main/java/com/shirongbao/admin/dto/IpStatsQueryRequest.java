/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 指定 IP 访问统计与行为追踪查询参数对象
 */
package com.shirongbao.admin.dto;

import com.shirongbao.common.constant.LogConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpStatsQueryRequest {
    private String ip;
    @Builder.Default
    private String range = LogConstants.RANGE_24H;

    // 获取统计时间跨度小时数
    public int getIntervalHours() {
        return LogConstants.RANGE_7D.equalsIgnoreCase(range) ? LogConstants.HOURS_168 : LogConstants.HOURS_24;
    }

    // 获取按时间分桶 SQL 表达式
    public String getTimeBucketExpr() {
        return LogConstants.RANGE_24H.equalsIgnoreCase(range)
                ? "to_char(created_at, 'YYYY-MM-DD HH24:MI')"
                : "to_char(created_at, 'YYYY-MM-DD HH24:00')";
    }
}
