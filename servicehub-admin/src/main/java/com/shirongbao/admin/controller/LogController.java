/*
 * auth: hirongbao
 * create: 2026-09-07
 * desc: 系统日志查询控制器，提供访问日志分页查询、流量统计聚合、运行日志尾部读取三个接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import com.shirongbao.common.utils.IpRegionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final JdbcTemplate jdbcTemplate;
    private final String logFilePath;

    // 初始化日志控制器
    public LogController(JdbcTemplate jdbcTemplate,
                         @Value("${server.port:8080}") int serverPort) {
        this.jdbcTemplate = jdbcTemplate;
        this.logFilePath = "logs/servicehub-" + serverPort + ".log";
    }

    // 分页查询访问日志，支持多维度筛选
    @GetMapping("/access")
    public ApiResponse<Map<String, Object>> accessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Integer statusMin,
            @RequestParam(required = false) Integer statusMax,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long minCostMs) {

        size = Math.max(1, Math.min(size, 100));
        int offset = (Math.max(1, page) - 1) * size;

        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (ip != null && !ip.isBlank()) {
            where.append(" AND ip_address LIKE ?");
            params.add("%" + ip.trim() + "%");
        }
        if (method != null && !method.isBlank()) {
            where.append(" AND method = ?");
            params.add(method.trim().toUpperCase());
        }
        if (path != null && !path.isBlank()) {
            where.append(" AND path LIKE ?");
            params.add("%" + path.trim() + "%");
        }
        if (statusCode != null) {
            where.append(" AND status_code = ?");
            params.add(statusCode);
        } else {
            if (statusMin != null) {
                where.append(" AND status_code >= ?");
                params.add(statusMin);
            }
            if (statusMax != null) {
                where.append(" AND status_code < ?");
                params.add(statusMax);
            }
        }
        if (startTime != null && !startTime.isBlank()) {
            where.append(" AND created_at >= ?");
            params.add(startTime.trim());
        }
        if (endTime != null && !endTime.isBlank()) {
            where.append(" AND created_at <= ?");
            params.add(endTime.trim());
        }
        if (minCostMs != null) {
            where.append(" AND cost_ms >= ?");
            params.add(minCostMs);
        }

        String countSql = "SELECT COUNT(*) FROM access_log " + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        String dataSql = "SELECT id, ip_address, method, path, query_string, status_code, cost_ms, user_agent, referer, created_at FROM access_log "
                + where + " ORDER BY id DESC LIMIT ? OFFSET ?";
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(offset);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(dataSql, dataParams.toArray());
        for (Map<String, Object> row : list) {
            String ipAddr = (String) row.get("ip_address");
            row.put("region", IpRegionUtils.getRegion(ipAddr));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total != null ? total : 0);
        result.put("page", page);
        result.put("size", size);
        return ApiResponse.success(result);
    }

    // 获取访问流量统计聚合数据
    @GetMapping("/access/stats")
    public ApiResponse<Map<String, Object>> accessStats(
            @RequestParam(defaultValue = "24h") String range) {

        String interval = "24h".equals(range) ? "24" : "168"; // 24h or 7d
        Map<String, Object> stats = new LinkedHashMap<>();

        // 基础统计：总请求、平均耗时、错误数、独立 IP
        Map<String, Object> summary = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS total_requests, " +
                "ROUND(AVG(cost_ms), 1) AS avg_cost_ms, " +
                "SUM(CASE WHEN status_code >= 400 THEN 1 ELSE 0 END) AS error_count, " +
                "COUNT(DISTINCT ip_address) AS unique_ips " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR)");
        stats.put("summary", summary);

        // 每小时请求量趋势
        List<Map<String, Object>> hourly = jdbcTemplate.queryForList(
                "SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00') AS hour, COUNT(*) AS count " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR) " +
                "GROUP BY hour ORDER BY hour");
        stats.put("hourlyTrend", hourly);

        // 状态码分布
        List<Map<String, Object>> statusDist = jdbcTemplate.queryForList(
                "SELECT CASE " +
                "WHEN status_code >= 200 AND status_code < 300 THEN '2xx' " +
                "WHEN status_code >= 300 AND status_code < 400 THEN '3xx' " +
                "WHEN status_code >= 400 AND status_code < 500 THEN '4xx' " +
                "WHEN status_code >= 500 THEN '5xx' ELSE 'other' END AS status_group, " +
                "COUNT(*) AS count " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR) " +
                "GROUP BY status_group ORDER BY status_group");
        stats.put("statusDistribution", statusDist);

        // Top 10 路径
        List<Map<String, Object>> topPaths = jdbcTemplate.queryForList(
                "SELECT path, COUNT(*) AS count, ROUND(AVG(cost_ms), 1) AS avg_ms " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR) " +
                "GROUP BY path ORDER BY count DESC LIMIT 10");
        stats.put("topPaths", topPaths);

        // Top 10 IP
        List<Map<String, Object>> topIps = jdbcTemplate.queryForList(
                "SELECT ip_address, COUNT(*) AS count, " +
                "MAX(created_at) AS last_seen " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR) " +
                "GROUP BY ip_address ORDER BY count DESC LIMIT 10");
        for (Map<String, Object> row : topIps) {
            String ipAddr = (String) row.get("ip_address");
            row.put("region", IpRegionUtils.getRegion(ipAddr));
        }
        stats.put("topIps", topIps);

        // 耗时分布
        List<Map<String, Object>> latencyDist = jdbcTemplate.queryForList(
                "SELECT CASE " +
                "WHEN cost_ms < 50 THEN '<50ms' " +
                "WHEN cost_ms < 200 THEN '50-200ms' " +
                "WHEN cost_ms < 500 THEN '200-500ms' " +
                "WHEN cost_ms < 1000 THEN '500ms-1s' " +
                "ELSE '>1s' END AS bracket, COUNT(*) AS count " +
                "FROM access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL " + interval + " HOUR) " +
                "GROUP BY bracket ORDER BY FIELD(bracket, '<50ms', '50-200ms', '200-500ms', '500ms-1s', '>1s')");
        stats.put("latencyDistribution", latencyDist);

        return ApiResponse.success(stats);
    }

    // 读取后端运行日志文件尾部内容
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
            result.put("message", "读取日志文件失败: " + e.getMessage());
            return ApiResponse.success(result);
        }
    }

    // 从文件尾部高效读取指定行数，使用 RandomAccessFile 倒序扫描换行符
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
