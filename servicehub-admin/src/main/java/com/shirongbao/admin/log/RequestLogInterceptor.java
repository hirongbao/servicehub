/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: HTTP 请求日志拦截器，记录接口、耗时、状态码和调用方身份
 */
package com.shirongbao.admin.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

public class RequestLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger("RequestLog");
    private static final String START_ATTR = RequestLogInterceptor.class.getName() + ".start";

    // 记录请求开始时间
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        return true;
    }

    // 请求结束后输出一条包含接口、耗时、状态码和调用方的日志
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_ATTR);
        long costMs = start instanceof Long t ? System.currentTimeMillis() - t : -1;
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            uri += "?" + query;
        }
        log.info("{} {} {} {}ms user={} token={}",
                request.getMethod(), uri, response.getStatus(), costMs,
                Objects.toString(request.getAttribute("auth.user"), "-"),
                Objects.toString(request.getAttribute("auth.tokenName"), "-"));
    }
}
