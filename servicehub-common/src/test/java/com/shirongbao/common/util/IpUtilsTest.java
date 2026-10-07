package com.shirongbao.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class IpUtilsTest {

    @Test
    void testDirectRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.195");
        assertEquals("203.0.113.195", IpUtils.getClientIp(request));
    }

    @Test
    void testIpv6MappedAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("::ffff:203.0.113.195");
        assertEquals("203.0.113.195", IpUtils.getClientIp(request));
    }

    @Test
    void testXForwardedForMultipleProxies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 10.0.0.1, 127.0.0.1");
        assertEquals("203.0.113.50", IpUtils.getClientIp(request));
    }

    @Test
    void testXForwardedForInternalFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
        // 当全部是私有 IP 时返回首个有效 IP
        assertEquals("192.168.1.100", IpUtils.getClientIp(request));
    }

    @Test
    void testXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Real-IP", "198.51.100.22");
        assertEquals("198.51.100.22", IpUtils.getClientIp(request));
    }

    @Test
    void testCfConnectingIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("CF-Connecting-IP", "198.51.100.99");
        assertEquals("198.51.100.99", IpUtils.getClientIp(request));
    }

    @Test
    void testIsInternalIp() {
        assertTrue(IpUtils.isInternalIp("127.0.0.1"));
        assertTrue(IpUtils.isInternalIp("::1"));
        assertTrue(IpUtils.isInternalIp("10.0.1.2"));
        assertTrue(IpUtils.isInternalIp("192.168.0.1"));
        assertTrue(IpUtils.isInternalIp("172.16.5.4"));
        assertFalse(IpUtils.isInternalIp("112.96.100.20"));
        assertFalse(IpUtils.isInternalIp("203.0.113.195"));
    }
}
