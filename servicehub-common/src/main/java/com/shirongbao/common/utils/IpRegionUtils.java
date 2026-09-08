package com.shirongbao.common.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.util.Arrays;

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

    /**
     * 解析 IP 返回格式化后的地址（如：中国 浙江省 杭州市 电信）
     */
    public static String getRegion(String ip) {
        if (ip == null || ip.trim().isEmpty() || searcher == null) {
            return "未知";
        }
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "本地环回";
        }
        try {
            String region = searcher.search(ip);
            // 默认格式：国家|区域|省份|城市|ISP，例如：中国|0|浙江省|杭州市|电信
            if (region != null) {
                // 替换掉 0 和 |
                String[] parts = region.split("\\|");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    if (!"0".equals(part) && !part.isEmpty()) {
                        sb.append(part).append(" ");
                    }
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            // 解析失败（例如 IPv6 或不合法 IP）
        }
        return "未知";
    }
}
