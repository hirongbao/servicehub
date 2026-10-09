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
import com.shirongbao.common.constant.LogConstants;
import com.shirongbao.common.util.IpUtils;
import com.shirongbao.hirongbaohub.security.UserContext;
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
        Long userId = resolveUserId(request);

        log.info("{} {} {} {}ms ip={} user={} uid={} token={}",
                request.getMethod(), fullPath, response.getStatus(), costMs, clientIp,
                Objects.toString(request.getAttribute("auth.user"), "-"),
                userId != null ? userId : "-",
                Objects.toString(request.getAttribute("auth.tokenName"), "-"));

        // 健康检查不持久化，避免无意义日志占用磁盘
        if (uri != null && !uri.equals(LogConstants.HEALTH_CHECK_PATH) && !uri.startsWith(LogConstants.HEALTH_CHECK_PATH + "/")) {
            String reqBody = null;
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    reqBody = new String(buf, StandardCharsets.UTF_8);
                    if (reqBody.length() > LogConstants.MAX_BODY_LOG_LENGTH) {
                        reqBody = reqBody.substring(0, LogConstants.MAX_BODY_LOG_LENGTH) + "...";
                    }
                }
            }
            String respBody = null;
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    respBody = new String(buf, StandardCharsets.UTF_8);
                    if (respBody.length() > LogConstants.MAX_BODY_LOG_LENGTH) {
                        respBody = respBody.substring(0, LogConstants.MAX_BODY_LOG_LENGTH) + "...";
                    }
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
            if (errorMsg != null && errorMsg.length() > LogConstants.MAX_ERROR_LOG_LENGTH) {
                errorMsg = errorMsg.substring(0, LogConstants.MAX_ERROR_LOG_LENGTH);
            }

            HttpRequestLog logRecord = HttpRequestLog.builder()
                    .clientIp(clientIp)
                    .method(request.getMethod())
                    .path(uri)
                    .queryParams(query)
                    .statusCode(response.getStatus())
                    .durationMs(costMs)
                    .userAgent(request.getHeader(LogConstants.HEADER_USER_AGENT))
                    .referer(request.getHeader(LogConstants.HEADER_REFERER))
                    .userId(userId)
                    .requestBody(reqBody)
                    .responseBody(respBody)
                    .errorMessage(errorMsg)
                    .build();

            logService.recordAsync(logRecord);
        }
    }

    // 解析当前请求的用户 ID，优先使用请求属性或上下文，兜底提取凭证并解析
    private Long resolveUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("auth.userId");
        if (userIdObj instanceof Long id) {
            return id;
        }
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        Long ctxUserId = UserContext.getUserId();
        if (ctxUserId != null) {
            return ctxUserId;
        }
        String credential = resolveCredential(request);
        if (credential == null || credential.isBlank()) {
            return null;
        }
        try {
            UserCredentialService.Parsed parsed = userCredentials.verifyAndParse(credential);
            if (parsed != null) {
                return parsed.userId();
            }
        } catch (Exception ignored) {
        }
        try {
            String adminUser = adminCredentials.resolveUsername(credential);
            if (adminUser != null) {
                return siteUserService.getAdminUserId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // 从请求头、查询参数或 Cookie 中解析用户或管理员凭证
    private String resolveCredential(HttpServletRequest request) {
        String authHeader = request.getHeader(LogConstants.HEADER_AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank()) {
            if (authHeader.startsWith(LogConstants.BEARER_PREFIX)) {
                return authHeader.substring(LogConstants.BEARER_PREFIX.length()).trim();
            }
            return authHeader.trim();
        }
        String saToken = request.getHeader(LogConstants.HEADER_SA_TOKEN);
        if (saToken != null && !saToken.isBlank()) {
            return saToken.trim();
        }
        String tokenHeader = request.getHeader("token");
        if (tokenHeader != null && !tokenHeader.isBlank()) {
            return tokenHeader.trim();
        }
        String xTokenHeader = request.getHeader("X-Token");
        if (xTokenHeader != null && !xTokenHeader.isBlank()) {
            return xTokenHeader.trim();
        }
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken.trim();
        }
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                String name = cookie.getName();
                if ("site_token".equals(name) || "token".equals(name) || "servicehub_token".equals(name)) {
                    if (cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        return cookie.getValue().trim();
                    }
                }
            }
        }
        return null;
    }
}
