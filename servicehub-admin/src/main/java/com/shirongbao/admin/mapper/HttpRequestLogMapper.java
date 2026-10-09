/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 请求日志持久化与聚合分析 MyBatis Mapper
 */
package com.shirongbao.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.shirongbao.admin.dto.AccessLogQueryRequest;
import com.shirongbao.admin.dto.IpAccessLogQueryRequest;
import com.shirongbao.admin.dto.IpStatsQueryRequest;
import com.shirongbao.admin.dto.LogStatsQueryRequest;
import com.shirongbao.admin.entity.HttpRequestLog;

@Mapper
@DS("pg")
public interface HttpRequestLogMapper {

    // 插入单条 HTTP 审计请求日志记录
    void insertLog(HttpRequestLog log);

    // 统计符合筛选条件的日志总数
    long countLogs(AccessLogQueryRequest query);

    // 分页查询符合条件的日志明细
    List<Map<String, Object>> selectLogs(AccessLogQueryRequest query);

    // 统计系统汇总数据（总请求、平均耗时、错误数、独立 IP 数）
    Map<String, Object> getSummary(LogStatsQueryRequest query);

    // 统计指定时间范围内每小时请求趋势
    List<Map<String, Object>> getHourlyTrend(LogStatsQueryRequest query);

    // 统计状态码分布（2xx, 3xx, 4xx, 5xx）
    List<Map<String, Object>> getStatusDistribution(LogStatsQueryRequest query);

    // 统计访问频次最高的 Top 10 Path
    List<Map<String, Object>> getTopPaths(LogStatsQueryRequest query);

    // 统计访问频次最高的 Top 10 IP
    List<Map<String, Object>> getTopIps(LogStatsQueryRequest query);

    // 统计接口耗时区间分布
    List<Map<String, Object>> getLatencyDistribution(LogStatsQueryRequest query);

    // 统计指定 IP 的请求汇总数据
    Map<String, Object> getIpSummary(IpStatsQueryRequest query);

    // 统计指定 IP 的状态码分布
    List<Map<String, Object>> getIpStatusDistribution(IpStatsQueryRequest query);

    // 统计指定 IP 访问频次最高的 Top 20 Path
    List<Map<String, Object>> getIpTopPaths(IpStatsQueryRequest query);

    // 统计指定 IP 随时间维度的频次分布
    List<Map<String, Object>> getIpTimeDistribution(IpStatsQueryRequest query);

    // 统计指定 IP 满足条件的日志总条数
    long countIpLogs(IpAccessLogQueryRequest query);

    // 分页查询指定 IP 满足条件的日志明细
    List<Map<String, Object>> selectIpLogs(IpAccessLogQueryRequest query);
}
