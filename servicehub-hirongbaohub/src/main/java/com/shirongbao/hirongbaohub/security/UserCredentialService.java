/*
 * auth: hirongbao
 * create: 2026-10-07
 * desc: 普通用户登录凭证签发与校验（HMAC-SHA256 无状态凭证）
 */
package com.shirongbao.hirongbaohub.security;

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
public class UserCredentialService {
    private static final long TTL_MILLIS = Duration.ofDays(30).toMillis();
    private final byte[] secret;

    // 初始化用户签名密钥，优先使用配置或共享密钥，未配置时自动持久化到本地文件
    public UserCredentialService(@Value("${servicehub.jwt.secret:}") String configuredSecret) {
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            this.secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
        } else {
            java.nio.file.Path secretFile = java.nio.file.Paths.get(".servicehub_user_secret");
            if (java.nio.file.Files.exists(secretFile)) {
                byte[] loaded = null;
                try {
                    loaded = java.nio.file.Files.readString(secretFile).trim().getBytes(StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
                this.secret = (loaded != null && loaded.length > 0) ? loaded : randomSecret().getBytes(StandardCharsets.UTF_8);
            } else {
                String generated = randomSecret();
                try {
                    java.nio.file.Files.writeString(secretFile, generated);
                } catch (Exception ignored) {}
                this.secret = generated.getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    // 签发用户登录凭证
    public String issue(Long userId, String role) {
        long expiresAt = System.currentTimeMillis() + TTL_MILLIS;
        String payload = userId + ":" + role + ":" + expiresAt;
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(payload);
    }

    // 校验并解析用户登录凭证
    public Parsed verifyAndParse(String credential) {
        if (credential == null || credential.isBlank()) return null;
        int dot = credential.lastIndexOf('.');
        if (dot <= 0 || dot == credential.length() - 1) return null;
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(credential.substring(0, dot)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        String[] parts = payload.split(":");
        if (parts.length != 3) return null;
        
        long expiresAt;
        try {
            expiresAt = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (expiresAt < System.currentTimeMillis()) return null;
        
        byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
        byte[] actual = credential.substring(dot + 1).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) return null;
        
        return new Parsed(Long.parseLong(parts[0]), parts[1], expiresAt);
    }

    // 计算凭证签名
    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("签名计算失败", e);
        }
    }

    // 生成随机密钥
    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public record Parsed(Long userId, String role, long expiresAt) {}
}
