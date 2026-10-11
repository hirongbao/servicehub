/*
 * auth: hirongbao
 * create: 2026-10-11
 * desc: IP 属地解析单元测试
 */
package com.shirongbao.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpRegionUtilsTest {

    // 测试国内与国外公网 IP 属地解析
    @Test
    void testPublicIp() {
        String region1 = IpRegionUtils.getRegion("115.196.75.244");
        String region2 = IpRegionUtils.getRegion("34.22.239.169");
        assertNotNull(region1);
        assertNotEquals("未知", region1);
        assertNotNull(region2);
        assertNotEquals("未知", region2);
    }

    // 测试带 /32 子网后缀的 IP 地址解析
    @Test
    void testIpWithSubnetMask() {
        String region1 = IpRegionUtils.getRegion("115.196.75.244/32");
        String region2 = IpRegionUtils.getRegion("34.22.239.169/32");
        assertNotNull(region1);
        assertNotEquals("未知", region1);
        assertNotNull(region2);
        assertNotEquals("未知", region2);
    }
}
