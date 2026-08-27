/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件业务服务
 */
package com.shirongbao.filehub.service;

import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.mapper.FileRecordMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    // 校验并上传图片文件
    public FileRecord upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择图片文件");
        if (file.getSize() > maxSize) throw new IllegalArgumentException("图片大小不能超过 10MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new IllegalArgumentException("只允许上传 JPG、PNG、GIF 或 WEBP 图片");
        String objectKey = cos.upload(file);
        FileRecord record = new FileRecord();
        record.setOriginalName(file.getOriginalFilename() == null || file.getOriginalFilename().isBlank() ? "image" : file.getOriginalFilename());
        record.setObjectKey(objectKey);
        record.setFileUrl(cos.publicUrl(objectKey));
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());
        record.setStatus(1);
        mapper.insert(record);
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
