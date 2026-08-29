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
    private static final long TTL_MILLIS = Duration.ofDays(30).toMillis();
    // 剩余有效期低于该阈值时在响应头下发新凭证，实现登录态滑动续期
    private static final long RENEW_THRESHOLD_MILLIS = Duration.ofDays(20).toMillis();
    private final byte[] secret;

    // 初始化签名密钥，未配置时使用进程内随机密钥（重启后凭证失效）
    public AdminCredentialService(@Value("${servicehub.admin.secret:}") String configuredSecret) {
        this.secret = (configuredSecret == null || configuredSecret.isBlank()
                ? randomSecret() : configuredSecret).getBytes(StandardCharsets.UTF_8);
    }

    // 签发管理员登录凭证，有效期 30 天
    public String issue(String username) {
        return issue(username, System.currentTimeMillis() + TTL_MILLIS);
    }

    // 凭证剩余有效期不足阈值时签发新凭证实现续期，否则返回 null
    public String renewIfEligible(String credential) {
        Parsed parsed = parse(credential);
        if (parsed == null || parsed.expiresAt() - System.currentTimeMillis() >= RENEW_THRESHOLD_MILLIS) {
            return null;
        }
        return issue(parsed.username());
    }

    // 校验管理员登录凭证，有效时返回凭证中的用户名
    public String resolveUsername(String credential) {
        Parsed parsed = parse(credential);
        return parsed == null ? null : parsed.username();
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

    // 解析并校验凭证，返回用户名与过期时间，无效时返回 null
    private Parsed parse(String credential) {
        if (!verify(credential)) {
            return null;
        }
        int dot = credential.lastIndexOf('.');
        String payload = new String(Base64.getUrlDecoder().decode(credential.substring(0, dot)), StandardCharsets.UTF_8);
        int colon = payload.lastIndexOf(':');
        return new Parsed(payload.substring(0, colon), Long.parseLong(payload.substring(colon + 1)));
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

    // 凭证解析结果：用户名与过期时间戳
    private record Parsed(String username, long expiresAt) {
    }
}
