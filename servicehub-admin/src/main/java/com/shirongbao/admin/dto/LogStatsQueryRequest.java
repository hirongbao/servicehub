/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 访问日志统计概览查询参数对象
 */
package com.shirongbao.admin.dto;

import com.shirongbao.common.constant.LogConstants;
import lombok.Data;

@Data
public class LogStatsQueryRequest {
    private String range = LogConstants.RANGE_24H;

    // 获取统计时间跨度小时数
    public int getIntervalHours() {
        return LogConstants.RANGE_7D.equalsIgnoreCase(range) ? LogConstants.HOURS_168 : LogConstants.HOURS_24;
    }
}
