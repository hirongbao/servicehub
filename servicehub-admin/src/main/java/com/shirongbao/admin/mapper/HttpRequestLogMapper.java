package com.shirongbao.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;

@Mapper
@DS("pg")
public interface HttpRequestLogMapper {

    void insertLog(@Param("clientIp") String clientIp,
                   @Param("method") String method,
                   @Param("path") String path,
                   @Param("queryParams") String queryParams,
                   @Param("statusCode") int statusCode,
                   @Param("durationMs") long durationMs,
                   @Param("userAgent") String userAgent,
                   @Param("referer") String referer,
                   @Param("userId") Long userId, @Param("requestBody") String requestBody, @Param("responseBody") String responseBody, @Param("errorMessage") String errorMessage);

    long countLogs(@Param("ip") String ip,
                   @Param("method") String method,
                   @Param("path") String path,
                   @Param("statusCode") Integer statusCode,
                   @Param("statusMin") Integer statusMin,
                   @Param("statusMax") Integer statusMax,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime,
                   @Param("minCostMs") Long minCostMs);

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

    Map<String, Object> getSummary(@Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getHourlyTrend(@Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getStatusDistribution(@Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getTopPaths(@Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getTopIps(@Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getLatencyDistribution(@Param("intervalHours") int intervalHours);

    Map<String, Object> getIpSummary(@Param("ip") String ip, @Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getIpStatusDistribution(@Param("ip") String ip, @Param("intervalHours") int intervalHours);
    List<Map<String, Object>> getIpTopPaths(@Param("ip") String ip, @Param("intervalHours") int intervalHours);
    
    // MyBatis can't easily dynamically replace SELECT columns without ${}, so we pass the expr
    List<Map<String, Object>> getIpTimeDistribution(@Param("ip") String ip, @Param("intervalHours") int intervalHours, @Param("timeBucketExpr") String timeBucketExpr);

    long countIpLogs(@Param("ip") String ip,
                     @Param("method") String method,
                     @Param("path") String path,
                     @Param("statusGroup") String statusGroup,
                     @Param("intervalHours") int intervalHours);

    List<Map<String, Object>> selectIpLogs(@Param("ip") String ip,
                                           @Param("method") String method,
                                           @Param("path") String path,
                                           @Param("statusGroup") String statusGroup,
                                           @Param("intervalHours") int intervalHours,
                                           @Param("offset") int offset,
                                           @Param("size") int size);
}

