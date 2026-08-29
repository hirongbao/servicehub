/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 管理员登录凭证签发与校验（HMAC-SHA256 无状态凭证）
 */
package com.shirongbao.admin.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.Base64;

@Component
public class AdminCredentialService {
    private static final long TTL_MILLIS = Duration.ofDays(7).toMillis();
    private final byte[] secret;

    // 初始化签名密钥，未配置时使用进程内随机密钥（重启后凭证失效）
    public AdminCredentialService(@Value("${servicehub.admin.secret:}") String configuredSecret) {
        this.secret = (configuredSecret == null || configuredSecret.isBlank()
                ? randomSecret() : configuredSecret).getBytes(StandardCharsets.UTF_8);
    }

    // 签发管理员登录凭证，有效期 7 天
    public String issue(String username) {
        return issue(username, System.currentTimeMillis() + TTL_MILLIS);
    }

    // 校验管理员登录凭证，有效时返回凭证中的用户名
    public String resolveUsername(String credential) {
        if (!verify(credential)) {
            return null;
        }
        int dot = credential.lastIndexOf('.');
        String payload = new String(Base64.getUrlDecoder().decode(credential.substring(0, dot)), StandardCharsets.UTF_8);
        return payload.substring(0, payload.lastIndexOf(':'));
    }

    // 校验管理员登录凭证是否有效
    public boolean verify(String credential) {
        int dot = credential == null ? -1 : credential.lastIndexOf('.');
        if (dot <= 0 || dot == credential.length() - 1) return false;
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(credential.substring(0, dot)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return false;
        }
        int colon = payload.lastIndexOf(':');
        if (colon <= 0) return false;
        long expiresAt;
        try {
            expiresAt = Long.parseLong(payload.substring(colon + 1));
        } catch (NumberFormatException e) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) return false;
        byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
        byte[] actual = credential.substring(dot + 1).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    // 按指定过期时间签发凭证
    String issue(String username, long expiresAtMillis) {
        String payload = username + ":" + expiresAtMillis;
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(payload);
    }

    // 计算 payload 的 HMAC-SHA256 签名
    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("签名计算失败", e);
        }
    }

    // 生成进程内随机密钥
    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
