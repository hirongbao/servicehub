/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 运行日志文件尾部内容读取查询参数对象
 */
package com.shirongbao.admin.dto;

import lombok.Data;

@Data
public class LogTailQueryRequest {
    private Integer lines = 200;
    private String keyword;

    // 获取受保护的读取行数（1 - 1000 行）
    public int getLimitLines() {
        int l = lines != null ? lines : 200;
        return Math.max(1, Math.min(l, 1000));
    }

    // 获取格式化后的搜索关键词
    public String getCleanKeyword() {
        return keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase() : null;
    }
}
