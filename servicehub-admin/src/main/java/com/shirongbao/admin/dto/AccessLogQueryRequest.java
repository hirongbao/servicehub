/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 访问日志分页查询参数对象
 */
package com.shirongbao.admin.dto;

import lombok.Data;

@Data
public class AccessLogQueryRequest {
    private Integer page = 1;
    private Integer size = 50;
    private String ip;
    private String method;
    private String path;
    private Integer statusCode;
    private Integer statusMin;
    private Integer minStatus;
    private Integer statusMax;
    private Integer maxStatus;
    private String startTime;
    private String endTime;
    private Long minCostMs;
    private Long minCost;

    // 获取生效的状态码下限
    public Integer getEffectiveStatusMin() {
        return statusMin != null ? statusMin : minStatus;
    }

    // 获取生效的状态码上限
    public Integer getEffectiveStatusMax() {
        return statusMax != null ? statusMax : maxStatus;
    }

    // 获取生效的最小耗时（毫秒）
    public Long getEffectiveMinCostMs() {
        return minCostMs != null ? minCostMs : minCost;
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

    // 获取格式化后的 IP 地址
    public String getCleanIp() {
        return ip != null && !ip.isBlank() ? ip.trim() : null;
    }

    // 获取格式化后的请求路径
    public String getCleanPath() {
        return path != null && !path.isBlank() ? path.trim() : null;
    }
}
