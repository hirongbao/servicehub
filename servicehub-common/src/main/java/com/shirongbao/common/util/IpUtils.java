/*
 * auth: hirongbao
 * create: 2026-09-07
 * desc: 客户端 IP 地址统一解析工具类，支持穿透多级反向代理、过滤私网跳数并兜底直连地址
 */
package com.shirongbao.common.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public final class IpUtils {
    private static final String UNKNOWN = "unknown";
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "X-Original-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    );

    private IpUtils() {}

    /**
     * 从 HTTP 请求中提取真实客户端 IP 地址。
     * 依次检查代理头（X-Forwarded-For、X-Real-IP、CF-Connecting-IP 等），
     * 自动穿透多层代理并过滤内部/回环/私网 IP，返回首个公网客户端 IP；
     * 若均为内网 IP 则返回首个有效 IP，兜底返回 request.getRemoteAddr()。
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        // 1. 优先扫描 X-Forwarded-For（支持多代理逗号拼接）
        String forwarded = request.getHeader("X-Forwarded-For");
        if (isValidHeader(forwarded)) {
            String[] ips = forwarded.split(",");
            // 优先寻找非内网公网 IP
            for (String rawIp : ips) {
                String ip = normalizeIp(rawIp);
                if (isValidIp(ip) && !isInternalIp(ip)) {
                    return ip;
                }
            }
            // 若全部为内网 IP，则取首个合法 IP
            for (String rawIp : ips) {
                String ip = normalizeIp(rawIp);
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }

        // 2. 依次检查其他单 IP 代理头
        for (String header : IP_HEADERS) {
            if ("X-Forwarded-For".equalsIgnoreCase(header)) continue;
            String val = request.getHeader(header);
            if (isValidHeader(val)) {
                String ip = normalizeIp(val.split(",")[0]);
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }

        // 3. 兜底取底层连接地址
        String remote = normalizeIp(request.getRemoteAddr());
        return isValidIp(remote) ? remote : UNKNOWN;
    }

    /**
     * 规范化 IP 地址：去除前后空白、清理 IPv4 映射的 IPv6 前缀（::ffff:）
     */
    public static String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "";
        }
        String value = ip.trim();
        if (value.startsWith("::ffff:")) {
            return value.substring(7);
        }
        return value;
    }

    /**
     * 基础格式校验
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank() || UNKNOWN.equalsIgnoreCase(ip)) {
            return false;
        }
        if (ip.length() > 45 || ip.contains("/") || ip.contains(" ") || ip.contains(";") || ip.contains("\n") || ip.contains("\r")) {
            return false;
        }
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断是否为内网/回环/保留地址
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String normalized = normalizeIp(ip);
        if ("127.0.0.1".equals(normalized) || "::1".equals(normalized) || "0:0:0:0:0:0:0:1".equals(normalized)) {
            return true;
        }
        try {
            InetAddress addr = InetAddress.getByName(normalized);
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static boolean isValidHeader(String value) {
        return value != null && !value.isBlank() && !UNKNOWN.equalsIgnoreCase(value.trim());
    }
}
