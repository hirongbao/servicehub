/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: AdminCredentialService 单元测试
 */
package com.shirongbao.admin.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminCredentialServiceTest {
    private final AdminCredentialService service = new AdminCredentialService("test-secret");

    // 签发的凭证应能通过校验
    @Test
    void issueThenVerify() {
        String credential = service.issue("hirongbao");
        assertTrue(service.verify(credential));
    }

    // 篡改签名后的凭证应被拒绝
    @Test
    void tamperedSignatureRejected() {
        String credential = service.issue("hirongbao");
        String tampered = credential.substring(0, credential.length() - 2) + "xx";
        assertFalse(service.verify(tampered));
    }

    // 已过期的凭证应被拒绝
    @Test
    void expiredCredentialRejected() {
        String expired = service.issue("hirongbao", System.currentTimeMillis() - 1000);
        assertFalse(service.verify(expired));
    }

    // 使用其他密钥签发的凭证应被拒绝
    @Test
    void wrongSecretRejected() {
        String credential = new AdminCredentialService("other-secret").issue("hirongbao");
        assertFalse(service.verify(credential));
    }

    // 非法格式的凭证应被拒绝
    @Test
    void malformedCredentialRejected() {
        assertFalse(service.verify(null));
        assertFalse(service.verify(""));
        assertFalse(service.verify("garbage"));
        assertFalse(service.verify("a.b"));
    }

    // 未配置密钥时每次启动应生成不同的随机密钥
    @Test
    void randomSecretDiffersBetweenInstances() {
        AdminCredentialService a = new AdminCredentialService("");
        AdminCredentialService b = new AdminCredentialService("");
        assertNotEquals(a.issue("hirongbao"), b.issue("hirongbao"));
        assertFalse(b.verify(a.issue("hirongbao")));
    }

    // 剩余有效期不足 20 天时应续期出新凭证
    @Test
    void renewalIssuedWhenNearExpiry() {
        String nearExpiry = service.issue("hirongbao", System.currentTimeMillis() + Duration.ofDays(5).toMillis());
        String renewed = service.renewIfEligible(nearExpiry);
        assertNotNull(renewed);
        assertTrue(service.verify(renewed));
        assertEquals("hirongbao", service.resolveUsername(renewed));
    }

    // 剩余有效期充足时不应续期
    @Test
    void renewalSkippedWhenFresh() {
        String fresh = service.issue("hirongbao", System.currentTimeMillis() + Duration.ofDays(25).toMillis());
        assertNull(service.renewIfEligible(fresh));
    }

    // 无效凭证不应触发续期
    @Test
    void renewalSkippedForInvalidCredential() {
        assertNull(service.renewIfEligible(null));
        assertNull(service.renewIfEligible("garbage"));
        assertNull(service.renewIfEligible(service.issue("hirongbao", System.currentTimeMillis() - 1000)));
    }
}
