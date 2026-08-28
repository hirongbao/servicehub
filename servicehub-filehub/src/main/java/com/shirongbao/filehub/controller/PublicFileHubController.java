/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 面向 FileHub Token 的公开文件接口
 */
package com.shirongbao.filehub.controller;

import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/filehub")
public class PublicFileHubController {
    private static final String HUB = "FILEHUB";
    private final FileRecordService service;
    private final ServiceTokenService tokenService;

    // 初始化公开 FileHub 接口
    public PublicFileHubController(FileRecordService service, ServiceTokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    // 查询文件列表
    @GetMapping
    public ApiResponse<List<FileRecord>> list(@RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                                              @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(serviceToken, authorization, "list");
        return ApiResponse.success(service.list());
    }

    // 使用 FileHub Token 上传图片
    @PostMapping("/upload")
    public ApiResponse<FileRecord> upload(@RequestPart("file") MultipartFile file,
                                          @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(serviceToken, authorization, "upload");
        return ApiResponse.success(service.upload(file));
    }

    // 使用 FileHub Token 删除图片
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(serviceToken, authorization, "delete");
        service.delete(id);
        return ApiResponse.success();
    }

    // 校验服务 Token 并记录使用日志
    private void recordUsage(String serviceToken, String authorization, String action) {
        String token = serviceToken;
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7).trim();
        }
        tokenService.recordUsage(tokenService.requireActive(token, HUB), action);
    }
}
