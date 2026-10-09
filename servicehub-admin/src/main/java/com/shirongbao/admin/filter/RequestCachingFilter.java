/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 请求与响应体缓存过滤器，支持安全提取 Body 用于日志审计且防止 OOM
 */
package com.shirongbao.admin.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCachingFilter implements Filter {

    // 过滤并包装 HTTP 请求与响应以支持读取流内容
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse res) {
            String uri = req.getRequestURI();
            // 危险：切勿拦截和缓存文件上传、下载等大二进制流接口，否则会导致 OOM
            boolean isFileUploadOrDownload = uri != null && (uri.contains("/upload") || uri.contains("/download") || uri.startsWith("/api/filehub/"));
            
            if (isFileUploadOrDownload) {
                chain.doFilter(request, response);
                return;
            }

            String contentType = req.getContentType();
            // 只缓存 JSON 或表单，文件上传等大包不缓存
            boolean shouldCacheRequest = contentType != null && 
                (contentType.contains("application/json") || contentType.contains("application/x-www-form-urlencoded"));
            
            HttpServletRequest requestToUse = shouldCacheRequest ? new ContentCachingRequestWrapper(req) : req;
            HttpServletResponse responseToUse = new ContentCachingResponseWrapper(res);
            
            chain.doFilter(requestToUse, responseToUse);
            
            // 重要：将响应内容写回输出流
            if (responseToUse instanceof ContentCachingResponseWrapper cachingResponse) {
                cachingResponse.copyBodyToResponse();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
