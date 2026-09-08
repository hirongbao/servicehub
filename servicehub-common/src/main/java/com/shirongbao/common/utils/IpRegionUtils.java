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

    /**
     * 解析 IP 返回格式化后的地址（如：杭州 (电信)）
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
                
                boolean hasCity = !"0".equals(city) && !city.isEmpty();
                if (hasCity) {
                    sb.append(city.replace("市", "")).append(" ");
                } else if (!"0".equals(province) && !province.isEmpty()) {
                    sb.append(province.replace("省", "").replace("市", "")).append(" ");
                }

                if (!"0".equals(isp) && !isp.isEmpty()) {
                    sb.append("(").append(isp).append(")");
                }
                
                String res = sb.toString().trim();
                return res.isEmpty() ? "未知" : res;
            }
        } catch (Exception e) {
            // 解析失败（例如 IPv6 或不合法 IP）
        }
        return "未知";
    }
}
