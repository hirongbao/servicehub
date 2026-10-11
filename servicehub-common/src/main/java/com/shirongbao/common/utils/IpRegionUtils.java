/*
 * auth: hirongbao
 * create: 2026-10-09
 * desc: 基于 ip2region 的 IP 属地离线解析工具类
 */
package com.shirongbao.common.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;

public class IpRegionUtils {
    private static Searcher searcher = null;

    static {
        try {
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            InputStream is = resource.getInputStream();
            byte[] cBuff = FileCopyUtils.copyToByteArray(is);
            searcher = Searcher.newWithBuffer(cBuff);
            is.close();
        } catch (Exception e) {
            System.err.println("Failed to load ip2region.xdb: " + e.getMessage());
        }
    }

    // 解析 IP 返回格式化后的地址（如：浙江 杭州 (电信)）
    public static String getRegion(String ip) {
        if (ip == null || ip.isBlank() || searcher == null) {
            return "未知";
        }
        ip = ip.trim();
        if (ip.contains("/")) {
            ip = ip.substring(0, ip.indexOf('/'));
        }
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1") || ip.equalsIgnoreCase("localhost")) {
            return "本地环回";
        }
        try {
            String region = searcher.search(ip);
            if (region != null) {
                String[] parts = region.split("\\|");
                String country = parts.length > 0 ? parts[0] : "";
                String province = parts.length > 1 ? parts[1] : "";
                String city = parts.length > 2 ? parts[2] : "";
                String isp = parts.length > 3 ? parts[3] : "";

                StringBuilder sb = new StringBuilder();
                if (!"中国".equals(country) && !"0".equals(country) && !country.isEmpty()) {
                    sb.append(country).append(" ");
                }

                if (!"0".equals(province) && !province.isEmpty()) {
                    province = province.replace("省", "").replace("市", "");
                    sb.append(province).append(" ");
                }

                if (!"0".equals(city) && !city.isEmpty()) {
                    city = city.replace("市", "");
                    if (!city.equals(province)) {
                        sb.append(city).append(" ");
                    }
                }

                if (!"0".equals(isp) && !isp.isEmpty()) {
                    sb.append("(").append(isp).append(")");
                }

                String res = sb.toString().trim();
                return res.isEmpty() ? "未知" : res;
            }
        } catch (Exception e) {
            // 解析失败（例如纯 IPv6 或不合法 IP）
        }
        return "未知";
    }
}
