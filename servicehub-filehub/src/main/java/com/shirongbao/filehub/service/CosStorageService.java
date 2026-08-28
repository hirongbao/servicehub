/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 腾讯 COS 文件存储服务
 */
package com.shirongbao.filehub.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CosStorageService {
    private final String secretId;
    private final String secretKey;
    private final String region;
    private final String bucket;
    private volatile COSClient client;

    public CosStorageService(@Value("${servicehub.cos.secret-id}") String secretId,
                             @Value("${servicehub.cos.secret-key}") String secretKey,
                             @Value("${servicehub.cos.region}") String region,
                             @Value("${servicehub.cos.bucket}") String bucket) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
    }

    // 上传图片到腾讯 COS
    public String upload(MultipartFile file) {
        String objectKey = "images/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        try {
            client().putObject(new PutObjectRequest(bucket, objectKey, file.getInputStream(), metadata));
        } catch (IOException exception) {
            throw new IllegalStateException("读取上传文件失败", exception);
        }
        return objectKey;
    }

    // 删除 COS 中的图片对象
    public void delete(String objectKey) {
        client().deleteObject(bucket, objectKey);
    }

    // 生成图片公开访问地址
    public String publicUrl(String objectKey) {
        return "https://" + bucket + ".cos." + region + ".myqcloud.com/" + objectKey;
    }

    // 应用关闭时释放 COS 客户端资源
    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.shutdown();
        }
    }

    // 获取复用的 COS 客户端，配置不完整时给出明确提示
    private COSClient client() {
        if (secretId.isBlank() || secretKey.isBlank() || bucket.isBlank()) {
            throw new IllegalStateException("COS 配置不完整，请检查 COS_SECRET_ID、COS_SECRET_KEY 和 COS_BUCKET");
        }
        COSClient current = client;
        if (current == null) {
            synchronized (this) {
                if (client == null) {
                    client = new COSClient(new BasicCOSCredentials(secretId, secretKey), new ClientConfig(new Region(region)));
                }
                current = client;
            }
        }
        return current;
    }

    // 获取文件扩展名
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
