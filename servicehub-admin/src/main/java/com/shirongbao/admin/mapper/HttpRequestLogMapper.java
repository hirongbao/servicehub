/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 请求日志持久化与聚合分析 MyBatis Mapper
 */
package com.shirongbao.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;

@Mapper
@DS("pg")
public interface HttpRequestLogMapper {

    // 插入单条 HTTP 审计请求日志记录
    void insertLog(@Param("clientIp") String clientIp,
                   @Param("method") String method,
                   @Param("path") String path,
                   @Param("queryParams") String queryParams,
                   @Param("statusCode") int statusCode,
                   @Param("durationMs") long durationMs,
                   @Param("userAgent") String userAgent,
                   @Param("referer") String referer,
                   @Param("userId") Long userId,
                   @Param("requestBody") String requestBody,
                   @Param("responseBody") String responseBody,
                   @Param("errorMessage") String errorMessage);

    // 统计符合筛选条件的日志总数
    long countLogs(@Param("ip") String ip,
                   @Param("method") String method,
                   @Param("path") String path,
                   @Param("statusCode") Integer statusCode,
                   @Param("statusMin") Integer statusMin,
                   @Param("statusMax") Integer statusMax,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime,
                   @Param("minCostMs") Long minCostMs);

    // 分页查询符合条件的日志明细
    List<Map<String, Object>> selectLogs(@Param("ip") String ip,
                                         @Param("method") String method,
                                         @Param("path") String path,
                                         @Param("statusCode") Integer statusCode,
                                         @Param("statusMin") Integer statusMin,
                                         @Param("statusMax") Integer statusMax,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("minCostMs") Long minCostMs,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    // 统计系统汇总数据（总请求、平均耗时、错误数、独立 IP 数）
    Map<String, Object> getSummary(@Param("intervalHours") int intervalHours);

    // 统计指定时间范围内每小时请求趋势
    List<Map<String, Object>> getHourlyTrend(@Param("intervalHours") int intervalHours);

    // 统计状态码分布（2xx, 3xx, 4xx, 5xx）
    List<Map<String, Object>> getStatusDistribution(@Param("intervalHours") int intervalHours);

    // 统计访问频次最高的 Top 10 Path
    List<Map<String, Object>> getTopPaths(@Param("intervalHours") int intervalHours);

    // 统计访问频次最高的 Top 10 IP
    List<Map<String, Object>> getTopIps(@Param("intervalHours") int intervalHours);

    // 统计接口耗时区间分布
    List<Map<String, Object>> getLatencyDistribution(@Param("intervalHours") int intervalHours);

    // 统计指定 IP 的请求汇总数据
    Map<String, Object> getIpSummary(@Param("ip") String ip, @Param("intervalHours") int intervalHours);

    // 统计指定 IP 的状态码分布
    List<Map<String, Object>> getIpStatusDistribution(@Param("ip") String ip, @Param("intervalHours") int intervalHours);

    // 统计指定 IP 访问频次最高的 Top 20 Path
    List<Map<String, Object>> getIpTopPaths(@Param("ip") String ip, @Param("intervalHours") int intervalHours);

    // 统计指定 IP 随时间维度的频次分布
    List<Map<String, Object>> getIpTimeDistribution(@Param("ip") String ip, @Param("intervalHours") int intervalHours, @Param("timeBucketExpr") String timeBucketExpr);

    // 统计指定 IP 满足条件的日志总条数
    long countIpLogs(@Param("ip") String ip,
                     @Param("method") String method,
                     @Param("path") String path,
                     @Param("statusGroup") String statusGroup,
                     @Param("intervalHours") int intervalHours);

    // 分页查询指定 IP 满足条件的日志明细
    List<Map<String, Object>> selectIpLogs(@Param("ip") String ip,
                                           @Param("method") String method,
                                           @Param("path") String path,
                                           @Param("statusGroup") String statusGroup,
                                           @Param("intervalHours") int intervalHours,
                                           @Param("offset") int offset,
                                           @Param("size") int size);
}
