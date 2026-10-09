/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 系统运行与 HTTP 访问审计日志管理控制器
 */
package com.shirongbao.admin.controller;

import lombok.RequiredArgsConstructor;

import com.shirongbao.admin.dto.*;
import com.shirongbao.admin.service.HttpRequestLogService;
import com.shirongbao.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final HttpRequestLogService logService;

    // 分页多条件查询 HTTP 访问审计日志
    @GetMapping("/access")
    public ApiResponse<AccessLogPageResponse> getAccessLogs(AccessLogQueryRequest request) {
        return ApiResponse.success(logService.getAccessLogs(request));
    }

    // 查询系统流量、状态码与耗时聚合统计概览
    @GetMapping("/access/stats")
    public ApiResponse<LogStatsResponse> getStats(LogStatsQueryRequest request) {
        return ApiResponse.success(logService.getStats(request));
    }

    // 查询指定 IP 的聚合统计与请求时间线分布
    @GetMapping({"/access/ip/{ip}/stats", "/access/ip/{ip}/trace"})
    public ApiResponse<IpStatsResponse> ipStats(@PathVariable String ip, IpStatsQueryRequest request) {
        request.setIp(ip);
        return ApiResponse.success(logService.getIpStats(request));
    }

    // 分页查询指定 IP 的请求明细日志
    @GetMapping("/access/ip/{ip}")
    public ApiResponse<AccessLogPageResponse> ipAccessLogs(@PathVariable String ip, IpAccessLogQueryRequest request) {
        request.setIp(ip);
        return ApiResponse.success(logService.getIpAccessLogs(request));
    }

    // 读取应用运行时日志文件的最新末尾内容
    @GetMapping("/tail")
    public ApiResponse<LogTailResponse> tailLog(LogTailQueryRequest request) {
        return ApiResponse.success(logService.tailLog(request));
    }
}
