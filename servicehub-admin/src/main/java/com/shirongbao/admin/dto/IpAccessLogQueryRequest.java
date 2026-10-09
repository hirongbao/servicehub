/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 指定 IP 访问日志明细分页查询参数对象
 */
package com.shirongbao.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpAccessLogQueryRequest {
    private String ip;
    private Integer page = 1;
    private Integer size = 50;
    private String range = "24h";
    private String method;
    private String path;
    private String statusGroup;

    // 获取统计时间跨度小时数
    public int getIntervalHours() {
        return "7d".equalsIgnoreCase(range) ? 168 : 24;
    }

    // 计算分页偏移量
    public int getOffset() {
        int p = page != null ? Math.max(1, page) : 1;
        return (p - 1) * getLimit();
    }

    // 获取受保护的分页大小（最大 200 条）
    public int getLimit() {
        int s = size != null ? size : 50;
        return Math.max(1, Math.min(s, 200));
    }

    // 获取格式化后的请求方法
    public String getCleanMethod() {
        return method != null && !method.isBlank() ? method.trim().toUpperCase() : null;
    }

    // 获取格式化后的请求路径
    public String getCleanPath() {
        return path != null && !path.isBlank() ? path.trim() : null;
    }

    // 获取格式化后的状态码分组
    public String getCleanStatusGroup() {
        return statusGroup != null && !statusGroup.isBlank() ? statusGroup.trim() : null;
    }
}
