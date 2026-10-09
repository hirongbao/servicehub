/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: HTTP 访问日志分页响应对象
 */
package com.shirongbao.admin.dto;

import java.util.List;
import java.util.Map;

public record AccessLogPageResponse(List<Map<String, Object>> list, long total, int page, int size) {
}
