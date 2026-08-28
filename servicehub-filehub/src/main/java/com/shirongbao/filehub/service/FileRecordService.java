/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件业务服务
 */
package com.shirongbao.filehub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.mapper.FileRecordMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Service
public class FileRecordService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private final FileRecordMapper mapper;
    private final CosStorageService cos;
    private final long maxSize;

    public FileRecordService(FileRecordMapper mapper, CosStorageService cos,
                             @Value("${servicehub.file.max-size}") long maxSize) {
        this.mapper = mapper;
        this.cos = cos;
        this.maxSize = maxSize;
    }

    // 查询文件记录列表
    public List<FileRecord> list() { return mapper.selectList(null); }

    // 校验并上传图片文件，内容重复时直接返回已有记录
    public FileRecord upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择图片文件");
        if (file.getSize() > maxSize) throw new IllegalArgumentException("图片大小不能超过 10MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new IllegalArgumentException("只允许上传 JPG、PNG、GIF 或 WEBP 图片");
        String hash = sha256(file);
        FileRecord existing = mapper.selectOne(new QueryWrapper<FileRecord>().eq("content_hash", hash));
        if (existing != null) return existing;
        String objectKey = cos.upload(file);
        FileRecord record = new FileRecord();
        record.setOriginalName(file.getOriginalFilename() == null || file.getOriginalFilename().isBlank() ? "image" : file.getOriginalFilename());
        record.setObjectKey(objectKey);
        record.setFileUrl(cos.publicUrl(objectKey));
        record.setContentType(file.getContentType());
        record.setContentHash(hash);
        record.setFileSize(file.getSize());
        record.setStatus(1);
        try {
            mapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 并发上传同一内容时，唯一索引兜底，返回已存在记录
            return mapper.selectOne(new QueryWrapper<FileRecord>().eq("content_hash", hash));
        }
        return record;
    }

    // 删除文件记录和 COS 对象
    public void delete(Long id) {
        FileRecord record = mapper.selectById(id);
        if (record == null) throw new IllegalArgumentException("文件不存在");
        cos.delete(record.getObjectKey());
        mapper.deleteById(id);
    }

    // 计算 SHA-256 内容哈希
    private String sha256(MultipartFile file) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("计算文件哈希失败", e);
        }
    }
}
