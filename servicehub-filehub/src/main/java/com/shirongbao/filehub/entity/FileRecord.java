/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件记录实体
 */
package com.shirongbao.filehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String objectKey;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
