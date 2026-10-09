/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: HTTP 请求日志拦截器，记录客户端真实 IP、接口、耗时、状态码和调用方身份，并异步持久化访问审计记录
 */
package com.shirongbao.admin.log;

import lombok.RequiredArgsConstructor;

import com.shirongbao.admin.entity.HttpRequestLog;
import com.shirongbao.admin.security.AdminCredentialService;
import com.shirongbao.admin.service.HttpRequestLogService;
import com.shirongbao.common.util.IpUtils;
import com.shirongbao.hirongbaohub.security.UserCredentialService;
import com.shirongbao.hirongbaohub.service.SiteUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RequestLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger("RequestLog");
    private static final String START_ATTR = RequestLogInterceptor.class.getName() + ".start";
    private final HttpRequestLogService logService;
    private final AdminCredentialService adminCredentials;
    private final UserCredentialService userCredentials;
    private final SiteUserService siteUserService;

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
        Long userId = null;
        Object userIdObj = request.getAttribute("auth.userId");
        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else {
            String credential = request.getHeader("Authorization");
            if (credential != null && credential.startsWith("Bearer ")) {
                credential = credential.substring(7).trim();
            } else {
                String legacy = request.getHeader("satoken");
                credential = legacy == null ? credential : legacy.trim();
            }
            if (credential != null && !credential.isEmpty()) {
                try {
                    String adminUser = adminCredentials.resolveUsername(credential);
                    if (adminUser != null) {
                        userId = siteUserService.getAdminUserId();
                    } else {
                        UserCredentialService.Parsed parsed = userCredentials.verifyAndParse(credential);
                        if (parsed != null) {
                            userId = parsed.userId();
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        log.info("{} {} {} {}ms ip={} user={} token={}",
                request.getMethod(), fullPath, response.getStatus(), costMs, clientIp,
                Objects.toString(request.getAttribute("auth.user"), "-"),
                Objects.toString(request.getAttribute("auth.tokenName"), "-"));

        // 健康检查不持久化，避免无意义日志占用磁盘
        if (uri != null && !uri.equals("/api/health") && !uri.startsWith("/api/health/")) {
            String reqBody = null;
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    reqBody = new String(buf, StandardCharsets.UTF_8);
                    if (reqBody.length() > 2000) reqBody = reqBody.substring(0, 2000) + "...";
                }
            }
            String respBody = null;
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    respBody = new String(buf, StandardCharsets.UTF_8);
                    if (respBody.length() > 2000) respBody = respBody.substring(0, 2000) + "...";
                }
            }
            String errorMsg = null;
            if (ex != null) {
                errorMsg = ex.getMessage();
            } else {
                Exception dispatchEx = (Exception) request.getAttribute("jakarta.servlet.error.exception");
                if (dispatchEx != null) errorMsg = dispatchEx.getMessage();
                else {
                    Object msg = request.getAttribute("jakarta.servlet.error.message");
                    if (msg != null) errorMsg = msg.toString();
                }
            }
            if (errorMsg != null && errorMsg.length() > 2000) errorMsg = errorMsg.substring(0, 2000);

            HttpRequestLog logRecord = HttpRequestLog.builder()
                    .clientIp(clientIp)
                    .method(request.getMethod())
                    .path(uri)
                    .queryParams(query)
                    .statusCode(response.getStatus())
                    .durationMs(costMs)
                    .userAgent(request.getHeader("User-Agent"))
                    .referer(request.getHeader("Referer"))
                    .userId(userId)
                    .requestBody(reqBody)
                    .responseBody(respBody)
                    .errorMessage(errorMsg)
                    .build();

            logService.recordAsync(logRecord);
        }
    }
}
