/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: FileHub 文件管理接口
 */
package com.shirongbao.filehub.controller;

import lombok.RequiredArgsConstructor;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileHubController {
    private final FileRecordService service;

    // 查询文件列表
    @GetMapping
    public ApiResponse<List<FileRecord>> list() { return ApiResponse.success(service.list()); }

    // 上传图片文件
    @PostMapping(value = "/upload", consumes = {"multipart/form-data", "*/*"})
    public ApiResponse<FileRecord> upload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String customKey) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空，请使用 multipart/form-data 表单并在 'file' 字段选择上传的文件");
        }
        return ApiResponse.success(service.upload(file, customKey));
    }

    // 删除图片文件
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
