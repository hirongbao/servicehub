/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 系统运行与 HTTP 访问审计日志管理控制器
 */
package com.shirongbao.admin.controller;

import lombok.RequiredArgsConstructor;

import com.shirongbao.admin.dto.*;
import com.shirongbao.admin.mapper.HttpRequestLogMapper;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.common.utils.IpRegionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final HttpRequestLogMapper logMapper;

    @Value("${logging.file.name:logs/servicehub-${server.port:8080}.log}")
    private String logFilePath;

    // 分页多条件查询 HTTP 访问审计日志
    @GetMapping("/access")
    public ApiResponse<AccessLogPageResponse> getAccessLogs(AccessLogQueryRequest request) {
        int limit = request.getLimit();
        int offset = request.getOffset();

        long total = logMapper.countLogs(
                request.getCleanIp(),
                request.getCleanMethod(),
                request.getCleanPath(),
                request.getStatusCode(),
                request.getEffectiveStatusMin(),
                request.getEffectiveStatusMax(),
                request.getStartTime(),
                request.getEndTime(),
                request.getEffectiveMinCostMs()
        );

        List<Map<String, Object>> list = logMapper.selectLogs(
                request.getCleanIp(),
                request.getCleanMethod(),
                request.getCleanPath(),
                request.getStatusCode(),
                request.getEffectiveStatusMin(),
                request.getEffectiveStatusMax(),
                request.getStartTime(),
                request.getEndTime(),
                request.getEffectiveMinCostMs(),
                offset,
                limit
        );

        for (Map<String, Object> row : list) {
            String ipAddr = (String) row.get("ip_address");
            if (ipAddr != null) {
                row.put("region", IpRegionUtils.getRegion(ipAddr));
            }
        }

        return ApiResponse.success(new AccessLogPageResponse(list, total, request.getPage(), limit));
    }

    // 查询系统流量、状态码与耗时聚合统计概览
    @GetMapping("/access/stats")
    public ApiResponse<LogStatsResponse> getStats(LogStatsQueryRequest request) {
        int intervalHours = request.getIntervalHours();

        Map<String, Object> summary = logMapper.getSummary(intervalHours);
        List<Map<String, Object>> hourlyTrend = logMapper.getHourlyTrend(intervalHours);
        List<Map<String, Object>> statusDistribution = logMapper.getStatusDistribution(intervalHours);
        List<Map<String, Object>> topPaths = logMapper.getTopPaths(intervalHours);

        List<Map<String, Object>> topIps = logMapper.getTopIps(intervalHours);
        for (Map<String, Object> row : topIps) {
            String ipAddr = (String) row.get("ip_address");
            row.put("region", IpRegionUtils.getRegion(ipAddr));
        }

        List<Map<String, Object>> latencyDistribution = logMapper.getLatencyDistribution(intervalHours);

        return ApiResponse.success(new LogStatsResponse(summary, hourlyTrend, statusDistribution, topPaths, topIps, latencyDistribution));
    }

    // 查询指定 IP 的聚合统计与请求时间线分布
    @GetMapping({"/access/ip/{ip}/stats", "/access/ip/{ip}/trace"})
    public ApiResponse<IpStatsResponse> ipStats(@PathVariable String ip, IpStatsQueryRequest request) {
        int intervalHours = request.getIntervalHours();
        String timeBucketExpr = request.getTimeBucketExpr();

        Map<String, Object> summary = logMapper.getIpSummary(ip, intervalHours);
        String region = IpRegionUtils.getRegion(ip);
        List<Map<String, Object>> statusDistribution = logMapper.getIpStatusDistribution(ip, intervalHours);
        List<Map<String, Object>> topPaths = logMapper.getIpTopPaths(ip, intervalHours);
        List<Map<String, Object>> timeDistribution = logMapper.getIpTimeDistribution(ip, intervalHours, timeBucketExpr);

        return ApiResponse.success(new IpStatsResponse(summary, region, statusDistribution, topPaths, timeDistribution));
    }

    // 分页查询指定 IP 的请求明细日志
    @GetMapping("/access/ip/{ip}")
    public ApiResponse<AccessLogPageResponse> ipAccessLogs(@PathVariable String ip, IpAccessLogQueryRequest request) {
        int limit = request.getLimit();
        int offset = request.getOffset();
        int intervalHours = request.getIntervalHours();

        long total = logMapper.countIpLogs(
                ip,
                request.getCleanMethod(),
                request.getCleanPath(),
                request.getCleanStatusGroup(),
                intervalHours
        );

        List<Map<String, Object>> list = logMapper.selectIpLogs(
                ip,
                request.getCleanMethod(),
                request.getCleanPath(),
                request.getCleanStatusGroup(),
                intervalHours,
                offset,
                limit
        );

        for (Map<String, Object> row : list) {
            String ipAddr = (String) row.get("ip_address");
            if (ipAddr != null) {
                row.put("region", IpRegionUtils.getRegion(ipAddr));
            }
        }

        return ApiResponse.success(new AccessLogPageResponse(list, total, request.getPage(), limit));
    }

    // 读取应用运行时日志文件的最新末尾内容
    @GetMapping("/tail")
    public ApiResponse<LogTailResponse> tailLog(LogTailQueryRequest request) {
        int lines = request.getLimitLines();
        Path logPath = Paths.get(logFilePath);

        if (!Files.exists(logPath)) {
            return ApiResponse.success(LogTailResponse.notFound(logFilePath));
        }

        try {
            long fileSize = Files.size(logPath);
            List<String> tailLines = readTailLines(logPath, lines);

            String keyword = request.getCleanKeyword();
            if (keyword != null) {
                tailLines = tailLines.stream()
                        .filter(line -> line.toLowerCase().contains(keyword))
                        .toList();
            }

            return ApiResponse.success(new LogTailResponse(tailLines, logFilePath, fileSize, tailLines.size(), null));
        } catch (IOException e) {
            return ApiResponse.success(LogTailResponse.notFound(logFilePath));
        }
    }

    // 从日志文件尾部倒序读取指定行数
    private List<String> readTailLines(Path filePath, int lineCount) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) return Collections.emptyList();

            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();
            long pos = fileLength - 1;

            while (pos >= 0 && lines.size() < lineCount) {
                raf.seek(pos);
                int ch = raf.read();
                if (ch == '\n') {
                    if (!currentLine.isEmpty() || pos < fileLength - 1) {
                        lines.add(currentLine.reverse().toString());
                        currentLine = new StringBuilder();
                    }
                } else if (ch != '\r') {
                    currentLine.append((char) ch);
                }
                pos--;
            }
            if (!currentLine.isEmpty() && lines.size() < lineCount) {
                lines.add(currentLine.reverse().toString());
            }
            Collections.reverse(lines);
            return lines;
        }
    }
}
