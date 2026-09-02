/*
 * auth: hirongbao
 * create: 2026-09-02
 * desc: 统一限制单 IP 请求频率并永久封禁超限地址
 */
package com.shirongbao.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(-100)
public class IpRateLimitFilter extends OncePerRequestFilter {
    private static final int SECOND_LIMIT = 50;
    private static final int MINUTE_LIMIT = 1000;
    private static final long SECOND_WINDOW_MS = 1_000L;
    private static final long MINUTE_WINDOW_MS = 60_000L;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> bannedIps = new ConcurrentHashMap<>();

    // 初始化 IP 限流过滤器
    public IpRateLimitFilter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // 对每个请求执行永久封禁检查和双窗口频率限制
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = resolveClientIp(request);
        // 本机回环地址用于 Nginx、健康检查和服务间调用，不参与公网限流
        if (isTrustedLocalIp(ip)) {
            chain.doFilter(request, response);
            return;
        }
        if (isBanned(ip)) {
            writeBlocked(response, "该 IP 地址已被永久封禁");
            return;
        }
        Counter counter = counters.computeIfAbsent(ip, ignored -> new Counter());
        String reason;
        synchronized (counter) {
            reason = counter.record(Instant.now().toEpochMilli());
        }
        if (reason != null) {
            ban(ip, reason);
            writeBlocked(response, "请求频率异常，该 IP 地址已被永久封禁");
            return;
        }
        chain.doFilter(request, response);
    }

    // 判断是否为本机回环地址
    private boolean isTrustedLocalIp(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    // 提取经过 Nginx 转发后的真实客户端 IP
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",", 2)[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        return real == null || real.isBlank() ? request.getRemoteAddr() : real.trim();
    }

    // 查询并缓存永久封禁地址
    private boolean isBanned(String ip) {
        if (bannedIps.containsKey(ip)) return true;
        Integer count;
        try {
            count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ip_ban WHERE ip_address = ?", Integer.class, ip);
        } catch (RuntimeException ignored) {
            // 数据库启动或迁移尚未完成时放行请求，避免影响应用启动
            return false;
        }
        if (count != null && count > 0) {
            bannedIps.put(ip, Boolean.TRUE);
            return true;
        }
        return false;
    }

    // 将超限地址写入数据库，保证重启后仍然永久生效
    private void ban(String ip, String reason) {
        jdbcTemplate.update("INSERT IGNORE INTO ip_ban (ip_address, reason) VALUES (?, ?)", ip, reason);
        bannedIps.put(ip, Boolean.TRUE);
    }

    // 返回统一的限流错误响应
    private void writeBlocked(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
    }

    private static final class Counter {
        private long secondStart;
        private long minuteStart;
        private int secondCount;
        private int minuteCount;

        // 记录请求并返回触发封禁的原因
        private String record(long now) {
            if (now - secondStart >= SECOND_WINDOW_MS) {
                secondStart = now;
                secondCount = 0;
            }
            if (now - minuteStart >= MINUTE_WINDOW_MS) {
                minuteStart = now;
                minuteCount = 0;
            }
            secondCount++;
            minuteCount++;
            if (secondCount > SECOND_LIMIT) return "1秒请求超过50次";
            if (minuteCount > MINUTE_LIMIT) return "1分钟请求超过1000次";
            return null;
        }
    }
}
