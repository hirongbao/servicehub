package com.shirongbao.admin.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import com.shirongbao.hirongbaohub.security.UserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts/ugc")
public class UserUgcFileController {

    private final FileRecordService fileRecordService;

    public UserUgcFileController(FileRecordService fileRecordService) {
        this.fileRecordService = fileRecordService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<FileRecord> upload(@RequestPart("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能上传图片");
        }
        // UGC 上传的图片来源标记为 UGC
        return ApiResponse.success(fileRecordService.upload(file, null, "UGC"));
    }
}
