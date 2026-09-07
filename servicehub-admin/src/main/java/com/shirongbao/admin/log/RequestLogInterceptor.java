/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: HTTP 请求日志拦截器，记录客户端真实 IP、接口、耗时、状态码和调用方身份，并异步持久化访问审计记录
 */
package com.shirongbao.admin.log;

import com.shirongbao.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger("RequestLog");
    private static final String START_ATTR = RequestLogInterceptor.class.getName() + ".start";
    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public RequestLogInterceptor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 记录请求开始时间
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        return true;
    }

    // 请求结束后输出一条包含客户端真实 IP、接口、耗时、状态码和调用方的日志，并异步落库
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_ATTR);
        long costMs = start instanceof Long t ? System.currentTimeMillis() - t : -1;
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = query != null ? uri + "?" + query : uri;
        String clientIp = IpUtils.getClientIp(request);

        log.info("{} {} {} {}ms ip={} user={} token={}",
                request.getMethod(), fullPath, response.getStatus(), costMs, clientIp,
                Objects.toString(request.getAttribute("auth.user"), "-"),
                Objects.toString(request.getAttribute("auth.tokenName"), "-"));

        // 健康检查不持久化，避免无意义日志占用磁盘
        if (uri != null && !uri.equals("/api/health") && !uri.startsWith("/api/health/")) {
            recordAccessLog(clientIp, request.getMethod(), uri, query, response.getStatus(), costMs,
                    request.getHeader("User-Agent"), request.getHeader("Referer"));
        }
    }

    // 异步记录访问日志至数据库，静默捕获异常防止影响业务
    private void recordAccessLog(String ip, String method, String path, String query,
                                int status, long costMs, String ua, String referer) {
        if (jdbcTemplate == null) return;
        asyncExecutor.execute(() -> {
            try {
                String safePath = path != null && path.length() > 512 ? path.substring(0, 512) : path;
                String safeQuery = query != null && query.length() > 1024 ? query.substring(0, 1024) : query;
                String safeUa = ua != null && ua.length() > 512 ? ua.substring(0, 512) : ua;
                String safeRef = referer != null && referer.length() > 512 ? referer.substring(0, 512) : referer;
                jdbcTemplate.update(
                        "INSERT INTO access_log (ip_address, method, path, query_string, status_code, cost_ms, user_agent, referer) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        ip, method, safePath, safeQuery, status, costMs, safeUa, safeRef
                );
            } catch (Exception ignored) {
                // 数据库迁移尚未完成或连接断开时不阻塞请求
            }
        });
    }
}
