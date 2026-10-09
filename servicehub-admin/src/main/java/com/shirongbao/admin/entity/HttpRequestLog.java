/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 访问审计日志实体对象
 */
package com.shirongbao.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestLog {
    private Long id;
    private Long userId;
    private String clientIp;
    private String method;
    private String path;
    private String queryParams;
    private String requestHeaders;
    private String requestBody;
    private Integer statusCode;
    private Long durationMs;
    private String responseBody;
    private String errorMessage;
    private String userAgent;
    private String referer;
    private LocalDateTime createdAt;
}
