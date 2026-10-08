package com.shirongbao.admin.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.admin.mapper.HttpRequestLogMapper;
import com.shirongbao.common.utils.IpRegionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/logs")
@lombok.RequiredArgsConstructor
public class LogController {
    
    private final HttpRequestLogMapper logMapper;
    
    @Value("${logging.file.name:logs/servicehub-${server.port:8080}.log}")
    private String logFilePath;


    @GetMapping("/access")
    public ApiResponse<Map<String, Object>> getAccessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Integer statusMin,
            @RequestParam(required = false) Integer statusMax,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long minCostMs) {

        size = Math.max(1, Math.min(size, 200));
        int offset = (Math.max(1, page) - 1) * size;
        
        long total = logMapper.countLogs(ip, method, path, statusCode, statusMin, statusMax, startTime, endTime, minCostMs);
        List<Map<String, Object>> list = logMapper.selectLogs(ip, method, path, statusCode, statusMin, statusMax, startTime, endTime, minCostMs, offset, size);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return ApiResponse.success(result);
    }

    @GetMapping("/access/stats")
    public ApiResponse<Map<String, Object>> getStats(@RequestParam(defaultValue = "24h") String range) {
        Map<String, Object> stats = new LinkedHashMap<>();
        int intervalHours = "24h".equals(range) ? 24 : 168;

        stats.put("summary", logMapper.getSummary(intervalHours));
        stats.put("hourlyTrend", logMapper.getHourlyTrend(intervalHours));
        stats.put("statusDistribution", logMapper.getStatusDistribution(intervalHours));
        stats.put("topPaths", logMapper.getTopPaths(intervalHours));
        
        List<Map<String, Object>> topIps = logMapper.getTopIps(intervalHours);
        for (Map<String, Object> row : topIps) {
            String ipAddr = (String) row.get("ip_address");
            row.put("region", IpRegionUtils.getRegion(ipAddr));
        }
        stats.put("topIps", topIps);
        
        stats.put("latencyDistribution", logMapper.getLatencyDistribution(intervalHours));
        return ApiResponse.success(stats);
    }

    @GetMapping("/access/ip/{ip}/trace")
    public ApiResponse<Map<String, Object>> ipTrace(
            @PathVariable String ip,
            @RequestParam(defaultValue = "24h") String range) {
        Map<String, Object> result = new LinkedHashMap<>();
        int intervalHours = "24h".equals(range) ? 24 : 168;
        String timeBucketExpr = "24h".equals(range) 
                ? "to_char(created_at, 'YYYY-MM-DD HH24:MI')" 
                : "to_char(created_at, 'YYYY-MM-DD HH24:00')";

        result.put("summary", logMapper.getIpSummary(ip, intervalHours));
        result.put("region", IpRegionUtils.getRegion(ip));
        result.put("statusDistribution", logMapper.getIpStatusDistribution(ip, intervalHours));
        result.put("topPaths", logMapper.getIpTopPaths(ip, intervalHours));
        result.put("timeDistribution", logMapper.getIpTimeDistribution(ip, intervalHours, timeBucketExpr));
        
        return ApiResponse.success(result);
    }

    @GetMapping("/access/ip/{ip}")
    public ApiResponse<Map<String, Object>> ipAccessLogs(
            @PathVariable String ip,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String statusGroup) {

        size = Math.max(1, Math.min(size, 200));
        int offset = (Math.max(1, page) - 1) * size;
        int intervalHours = "24h".equals(range) ? 24 : 168;

        long total = logMapper.countIpLogs(ip, method, path, statusGroup, intervalHours);
        List<Map<String, Object>> list = logMapper.selectIpLogs(ip, method, path, statusGroup, intervalHours, offset, size);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return ApiResponse.success(result);
    }

    @GetMapping("/tail")
    public ApiResponse<Map<String, Object>> tailLog(
            @RequestParam(defaultValue = "200") int lines,
            @RequestParam(required = false) String keyword) {

        lines = Math.max(1, Math.min(lines, 1000));
        Path logPath = Paths.get(logFilePath);
        Map<String, Object> result = new LinkedHashMap<>();

        if (!Files.exists(logPath)) {
            result.put("lines", Collections.emptyList());
            result.put("fileName", logFilePath);
            result.put("fileSize", 0);
            result.put("message", "日志文件不存在");
            return ApiResponse.success(result);
        }

        try {
            long fileSize = Files.size(logPath);
            List<String> tailLines = readTailLines(logPath, lines);

            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.trim().toLowerCase();
                tailLines = tailLines.stream()
                        .filter(line -> line.toLowerCase().contains(kw))
                        .toList();
            }

            result.put("lines", tailLines);
            result.put("fileName", logFilePath);
            result.put("fileSize", fileSize);
            result.put("totalLines", tailLines.size());
            return ApiResponse.success(result);
        } catch (IOException e) {
            result.put("lines", Collections.emptyList());
            result.put("fileName", logFilePath);
            result.put("message", "日志文件不存在");
            return ApiResponse.success(result);
        }
    }

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
