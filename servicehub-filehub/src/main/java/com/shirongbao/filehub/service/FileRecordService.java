/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件业务服务
 */
package com.shirongbao.filehub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.mapper.FileRecordMapper;
import com.shirongbao.filehub.util.ContentHash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // 查询文件记录列表，并将对象地址动态转为当前配置的加速域名
    public List<FileRecord> list() {
        List<FileRecord> records = mapper.selectList(null);
        for (FileRecord r : records) {
            if (r.getObjectKey() != null && !r.getObjectKey().isBlank()) {
                r.setFileUrl(cos.publicUrl(r.getObjectKey()));
            }
        }
        return records;
    }

    // 统计文件记录总数
    public long countAll() { return mapper.selectCount(null); }

    // 校验并上传图片文件，支持自定义固定标识实现覆盖和 URL 不变
    public FileRecord upload(MultipartFile file, String customKey) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择图片文件");
        if (file.getSize() > maxSize) throw new IllegalArgumentException("图片大小不能超过 10MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new IllegalArgumentException("只允许上传 JPG、PNG、GIF 或 WEBP 图片");
        String hash;
        try {
            hash = ContentHash.of(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        }

        if (customKey != null && !customKey.isBlank()) {
            // 固定 URL 模式：直接基于自定义 Key 生成 objectKey
            String objectKey = "fixed/" + customKey.trim().replaceAll("^/+", "");
            // 为了防止和普通上传记录的唯一 Hash 冲突，这里对自定义 Key 的文件做专属 Hash 处理
            String uniqueHash = hash + "_" + customKey.trim();

            cos.upload(file, customKey);

            FileRecord existingByKey = mapper.selectOne(new QueryWrapper<FileRecord>().eq("object_key", objectKey));
            if (existingByKey != null) {
                // 原有的记录存在，直接覆盖更新属性
                existingByKey.setContentHash(uniqueHash);
                existingByKey.setFileSize(file.getSize());
                existingByKey.setContentType(file.getContentType());
                existingByKey.setFileUrl(cos.publicUrl(objectKey));
                mapper.updateById(existingByKey);
                return existingByKey;
            } else {
                // 全新固定 URL 图片
                FileRecord record = new FileRecord();
                record.setOriginalName(file.getOriginalFilename() == null || file.getOriginalFilename().isBlank() ? "image" : file.getOriginalFilename());
                record.setObjectKey(objectKey);
                record.setFileUrl(cos.publicUrl(objectKey));
                record.setContentType(file.getContentType());
                record.setContentHash(uniqueHash);
                record.setFileSize(file.getSize());
                record.setStatus(1);
                mapper.insert(record);
                return record;
            }
        }

        FileRecord existing = mapper.selectOne(new QueryWrapper<FileRecord>().eq("content_hash", hash));
        if (existing != null) {
            String expectedUrl = cos.publicUrl(existing.getObjectKey());
            if (!expectedUrl.equals(existing.getFileUrl())) {
                existing.setFileUrl(expectedUrl);
                mapper.updateById(existing);
            }
            return existing;
        }
        String objectKey = cos.upload(file, null);
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
            FileRecord dupe = mapper.selectOne(new QueryWrapper<FileRecord>().eq("content_hash", hash));
            if (dupe != null) {
                String expectedUrl = cos.publicUrl(dupe.getObjectKey());
                if (!expectedUrl.equals(dupe.getFileUrl())) {
                    dupe.setFileUrl(expectedUrl);
                    mapper.updateById(dupe);
                }
            }
            return dupe;
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
}