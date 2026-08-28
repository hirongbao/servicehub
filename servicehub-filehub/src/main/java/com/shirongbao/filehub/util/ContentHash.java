/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: SHA-256 内容哈希工具
 */
package com.shirongbao.filehub.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ContentHash {
    private ContentHash() {
    }

    // 计算字节数组的 SHA-256 哈希
    public static String of(byte[] data) {
        return HexFormat.of().formatHex(sha256().digest(data));
    }

    // 计算输入流的 SHA-256 哈希
    public static String of(InputStream in) {
        MessageDigest digest = sha256();
        byte[] buffer = new byte[8192];
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取内容失败", e);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    // 获取 SHA-256 摘要实例
    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}
