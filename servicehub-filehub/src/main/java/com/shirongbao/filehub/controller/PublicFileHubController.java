/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 面向 FileHub Token 的公开文件接口
 */
package com.shirongbao.filehub.controller;

import com.shirongbao.authhub.entity.ServiceToken;
import jakarta.servlet.http.HttpServletRequest;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "FileHub 开放接口", description = "供外部调用的媒体资产管理 API。支持通过 X-Service-Token 或 Bearer Token 鉴权。")
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

    // 使用 FileHub Token 上传图片
    @Operation(summary = "上传媒体文件", description = "支持上传单张图片或文件。支持格式：jpg/png/webp/gif 等。")
    @SecurityRequirement(name = "X-Service-Token")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<FileRecord> upload(
            @Parameter(description = "媒体文件") @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(description = "通过自定义 Header 传递访问凭证") @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @Parameter(description = "通过标准 Authorization Header 传递凭证 (Bearer xxx)") @RequestHeader(value = "Authorization", required = false) String authorization) {
        recordUsage(request, serviceToken, authorization, "upload");
        return ApiResponse.success(service.upload(file));
    }

    // 校验服务 Token 并记录使用日志
    private void recordUsage(HttpServletRequest request, String serviceToken, String authorization, String action) {
        String token = serviceToken;
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7).trim();
        }
        ServiceToken serviceTokenEntity = tokenService.requireActive(token, HUB);
        request.setAttribute("auth.tokenName", serviceTokenEntity.getTokenName());
        tokenService.recordUsage(serviceTokenEntity, action);
    }
}
