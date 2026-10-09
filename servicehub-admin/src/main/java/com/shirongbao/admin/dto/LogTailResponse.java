/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 运行日志文件尾部内容读取响应对象
 */
package com.shirongbao.admin.dto;

import java.util.List;

public record LogTailResponse(List<String> lines, String fileName, long fileSize, int totalLines, String message) {
    // 快速构建日志文件不存在的默认响应
    public static LogTailResponse notFound(String fileName) {
        return new LogTailResponse(List.of(), fileName, 0L, 0, "日志文件不存在");
    }
}
