/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 服务健康检查接口
 */
package com.shirongbao.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
}
