/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 运行日志文件尾部内容读取查询参数对象
 */
package com.shirongbao.admin.dto;

import com.shirongbao.common.constant.LogConstants;
import lombok.Data;

@Data
public class LogTailQueryRequest {
    private Integer lines = LogConstants.DEFAULT_TAIL_LINES;
    private String keyword;

    // 获取受保护的读取行数（1 - 1000 行）
    public int getLimitLines() {
        int l = lines != null ? lines : LogConstants.DEFAULT_TAIL_LINES;
        return Math.max(1, Math.min(l, LogConstants.MAX_TAIL_LINES));
    }

    // 获取格式化后的搜索关键词
    public String getCleanKeyword() {
        return keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase() : null;
    }
}
