/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 服务健康检查与客户端 IP 诊断接口
 */
package com.shirongbao.admin.controller;

import com.shirongbao.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final JdbcTemplate jdbc;

    // 初始化健康检查接口
    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 探测数据库连通性并返回服务健康状态
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        String time = LocalDateTime.now().toString();
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "UP", "database", "UP", "time", time));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DOWN", "database", "DOWN", "time", time));
        }
    }

    // 诊断接口：检查后端识别出的客户端真实 IP、连接对端地址与关键代理头
    @GetMapping("/ip")
    public ResponseEntity<Map<String, Object>> checkIp(HttpServletRequest request) {
        String clientIp = IpUtils.getClientIp(request);
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String lower = name.toLowerCase();
                if (lower.contains("ip") || lower.contains("forward") || lower.contains("host") || lower.contains("user-agent")) {
                    headers.put(name, request.getHeader(name));
                }
            }
        }
        return ResponseEntity.ok(Map.of(
                "clientIp", clientIp,
                "remoteAddr", String.valueOf(request.getRemoteAddr()),
                "isInternal", IpUtils.isInternalIp(clientIp),
                "proxyHeaders", headers,
                "time", LocalDateTime.now().toString()
        ));
    }
}
