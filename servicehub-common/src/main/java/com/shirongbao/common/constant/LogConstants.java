/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 全局日志常量定义
 */
package com.shirongbao.common.constant;

public final class LogConstants {

    // 私有构造函数，防止实例化
    private LogConstants() {
    }

    // 日志字段键名：IP 地址
    public static final String FIELD_IP_ADDRESS = "ip_address";

    // 日志字段键名：归属地区
    public static final String FIELD_REGION = "region";

    // 时间范围常量：24 小时
    public static final String RANGE_24H = "24h";

    // 时间范围常量：7 天
    public static final String RANGE_7D = "7d";

    // 小时数常量：24 小时
    public static final int HOURS_24 = 24;

    // 小时数常量：168 小时（7 天）
    public static final int HOURS_168 = 168;

    // 分页默认页码
    public static final int DEFAULT_PAGE = 1;

    // 分页默认每页条数
    public static final int DEFAULT_PAGE_SIZE = 50;

    // 分页最大保护条数
    public static final int MAX_PAGE_SIZE = 200;

    // 运行日志末尾默认读取行数
    public static final int DEFAULT_TAIL_LINES = 200;

    // 运行日志末尾最大读取行数
    public static final int MAX_TAIL_LINES = 1000;

    // 路径存储最大字符截断长度
    public static final int MAX_PATH_LENGTH = 512;

    // 查询参数存储最大字符截断长度
    public static final int MAX_QUERY_LENGTH = 1024;

    // 请求头存储最大字符截断长度
    public static final int MAX_HEADER_LENGTH = 512;

    // 请求/响应体记录最大截断长度
    public static final int MAX_BODY_LOG_LENGTH = 2000;

    // 错误信息记录最大截断长度
    public static final int MAX_ERROR_LOG_LENGTH = 2000;

    // HTTP 请求头：User-Agent
    public static final String HEADER_USER_AGENT = "User-Agent";

    // HTTP 请求头：Referer
    public static final String HEADER_REFERER = "Referer";

    // HTTP 请求头：Authorization
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // HTTP 请求头：satoken
    public static final String HEADER_SA_TOKEN = "satoken";

    // Bearer Token 前缀
    public static final String BEARER_PREFIX = "Bearer ";

    // 健康检查 API 路径
    public static final String HEALTH_CHECK_PATH = "/api/health";
}
