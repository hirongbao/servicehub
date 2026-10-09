/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 请求审计日志与系统日志业务服务
 */
package com.shirongbao.admin.service;

import lombok.RequiredArgsConstructor;
import com.shirongbao.admin.dto.*;
import com.shirongbao.admin.entity.HttpRequestLog;
import com.shirongbao.admin.mapper.HttpRequestLogMapper;
import com.shirongbao.common.utils.IpRegionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class HttpRequestLogService {

    private final HttpRequestLogMapper logMapper;
    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Value("${logging.file.name:logs/servicehub-${server.port:8080}.log}")
    private String logFilePath;

    // 异步持久化单条请求日志
    public void recordAsync(HttpRequestLog log) {
        if (logMapper == null || log == null) return;
        asyncExecutor.execute(() -> {
            try {
                if (log.getPath() != null && log.getPath().length() > 512) {
                    log.setPath(log.getPath().substring(0, 512));
                }
                if (log.getQueryParams() != null && log.getQueryParams().length() > 1024) {
                    log.setQueryParams(log.getQueryParams().substring(0, 1024));
                }
                if (log.getUserAgent() != null && log.getUserAgent().length() > 512) {
                    log.setUserAgent(log.getUserAgent().substring(0, 512));
                }
                if (log.getReferer() != null && log.getReferer().length() > 512) {
                    log.setReferer(log.getReferer().substring(0, 512));
                }
                logMapper.insertLog(log);
            } catch (Exception ignored) {
            }
        });
    }

    // 分页查询 HTTP 请求访问日志列表并解析 IP 归属地
    public AccessLogPageResponse getAccessLogs(AccessLogQueryRequest request) {
        long total = logMapper.countLogs(request);
        List<Map<String, Object>> list = logMapper.selectLogs(request);

        for (Map<String, Object> row : list) {
            String ipAddr = (String) row.get("ip_address");
            if (ipAddr != null) {
                row.put("region", IpRegionUtils.getRegion(ipAddr));
            }
        }

        return new AccessLogPageResponse(list, total, request.getPage(), request.getLimit());
    }

    // 查询系统流量、状态码与耗时聚合统计概览
    public LogStatsResponse getStats(LogStatsQueryRequest request) {
        Map<String, Object> summary = logMapper.getSummary(request);
        List<Map<String, Object>> hourlyTrend = logMapper.getHourlyTrend(request);
        List<Map<String, Object>> statusDistribution = logMapper.getStatusDistribution(request);
        List<Map<String, Object>> topPaths = logMapper.getTopPaths(request);

        List<Map<String, Object>> topIps = logMapper.getTopIps(request);
        for (Map<String, Object> row : topIps) {
            String ipAddr = (String) row.get("ip_address");
            row.put("region", IpRegionUtils.getRegion(ipAddr));
        }

        List<Map<String, Object>> latencyDistribution = logMapper.getLatencyDistribution(request);

        return new LogStatsResponse(summary, hourlyTrend, statusDistribution, topPaths, topIps, latencyDistribution);
    }

    // 查询指定 IP 的聚合统计与请求时间线分布
    public IpStatsResponse getIpStats(IpStatsQueryRequest request) {
        Map<String, Object> summary = logMapper.getIpSummary(request);
        String region = IpRegionUtils.getRegion(request.getIp());
        List<Map<String, Object>> statusDistribution = logMapper.getIpStatusDistribution(request);
        List<Map<String, Object>> topPaths = logMapper.getIpTopPaths(request);
        List<Map<String, Object>> timeDistribution = logMapper.getIpTimeDistribution(request);

        return new IpStatsResponse(summary, region, statusDistribution, topPaths, timeDistribution);
    }

    // 分页查询指定 IP 的请求明细日志并解析 IP 归属地
    public AccessLogPageResponse getIpAccessLogs(IpAccessLogQueryRequest request) {
        long total = logMapper.countIpLogs(request);
        List<Map<String, Object>> list = logMapper.selectIpLogs(request);

        for (Map<String, Object> row : list) {
            String ipAddr = (String) row.get("ip_address");
            if (ipAddr != null) {
                row.put("region", IpRegionUtils.getRegion(ipAddr));
            }
        }

        return new AccessLogPageResponse(list, total, request.getPage(), request.getLimit());
    }

    // 实时读取应用运行时日志文件末尾内容
    public LogTailResponse tailLog(LogTailQueryRequest request) {
        int lines = request.getLimitLines();
        Path logPath = Paths.get(logFilePath);

        if (!Files.exists(logPath)) {
            return LogTailResponse.notFound(logFilePath);
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

            return new LogTailResponse(tailLines, logFilePath, fileSize, tailLines.size(), null);
        } catch (IOException e) {
            return LogTailResponse.notFound(logFilePath);
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
