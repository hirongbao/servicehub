/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件管理接口
 */
package com.shirongbao.filehub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileHubController {
    private final FileRecordService service;

    // 初始化文件管理服务
    public FileHubController(FileRecordService service) {
        this.service = service;
    }

    // 查询文件列表
    @GetMapping
    public ApiResponse<List<FileRecord>> list() { return ApiResponse.success(service.list()); }

    // 上传图片文件
    @PostMapping("/upload")
    public ApiResponse<FileRecord> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(service.upload(file));
    }

    // 删除图片文件
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
