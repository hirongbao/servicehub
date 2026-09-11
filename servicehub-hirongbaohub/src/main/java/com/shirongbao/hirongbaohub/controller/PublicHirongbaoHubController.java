/*
 * auth: hirongbao
 * create: 2026-09-08
 * desc: 面向 HirongbaoHub Token 的公开动态发布接口
 */
package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.authhub.entity.ServiceToken;
import com.shirongbao.authhub.service.ServiceTokenService;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.service.SitePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.shirongbao.hirongbaohub.service.SiteProfileService;
import com.shirongbao.hirongbaohub.entity.SiteProfile;
import com.shirongbao.hirongbaohub.entity.SitePostMedia;


@Tag(name = "HirongbaoHub \u52a8\u6001\u53d1\u5e03\u63a5\u53e3", description = "\u4f9b\u5916\u90e8 Agent \u8c03\u7528\u7684\u52a8\u6001\u53d1\u5e03 API\u3002\u4f7f\u7528 HIRONGBAOHUB \u7c7b\u578b\u7684 Token \u8fdb\u884c\u9274\u6743\u3002\n\n"
        + "## \u5206\u7c7b\u8bf4\u660e\n\n"
        + "| categoryId | categoryName | \u9002\u7528\u573a\u666f |\n"
        + "|------------|-------------|----------|\n"
        + "| `food` | \u7f8e\u98df | \u7f8e\u98df\u63a2\u5e97\u3001\u5403\u996d\u6253\u5361\u3001\u98df\u8c31\u5206\u4eab |\n"
        + "| `scenery` | \u98ce\u666f | \u65c5\u884c\u98ce\u5149\u3001\u6237\u5916\u6444\u5f71\u3001\u57ce\u5e02\u666f\u89c2 |\n"
        + "| `notes` | \u968f\u7b14 | \u65e5\u5e38\u968f\u60f3\u3001\u6280\u672f\u7b14\u8bb0\u3001\u5176\u4ed6\u5185\u5bb9\uff08\u9ed8\u8ba4\uff09 |\n\n"
        + "\u82e5\u4e0d\u4f20 `categoryId`\uff0c\u9ed8\u8ba4\u5f52\u7c7b\u4e3a `notes`\uff08\u968f\u7b14\uff09\u3002\n\n"
        + "## \u53d1\u5e03\u5e26\u56fe\u52a8\u6001\u7684\u5b8c\u6574\u6d41\u7a0b\n\n"
        + "1. \u8c03\u7528 `POST /api/filehub/upload` \u4e0a\u4f20\u56fe\u7247\uff08\u4f7f\u7528 FILEHUB Token\uff09\uff0c\u83b7\u53d6\u8fd4\u56de\u7684 `url` \u5b57\u6bb5\n"
        + "2. \u8c03\u7528\u672c\u63a5\u53e3\u53d1\u5e03\u52a8\u6001\uff0c\u5c06\u56fe\u7247 URL \u586b\u5165 `mediaUrls` \u6570\u7ec4")
@RestController
@RequestMapping("/api/hirongbaohub")
public class PublicHirongbaoHubController {
    private static final String HUB = "HIRONGBAOHUB";
    private final SitePostService postService;
    private final ServiceTokenService tokenService;
    private final SiteProfileService profileService;

    public PublicHirongbaoHubController(SitePostService postService, ServiceTokenService tokenService, SiteProfileService profileService) {
        this.postService = postService;
        this.tokenService = tokenService;
        this.profileService = profileService;
    }

    @Operation(
            summary = "\u53d1\u5e03\u52a8\u6001",
            description = "\u901a\u8fc7 Token \u9274\u6743\u53d1\u5e03\u4e00\u6761\u52a8\u6001\u3002\u5185\u5bb9\u4e0e\u5a92\u4f53\u81f3\u5c11\u586b\u4e00\u4e2a\uff0c\u56fe\u7247\u6700\u591a 9 \u5f20\uff0c\u89c6\u9891\u6700\u591a 1 \u6761\u3002",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostUpsertRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "\u7eaf\u6587\u5b57\u52a8\u6001",
                                            value = "{\"content\": \"\u4eca\u5929\u5929\u6c14\u771f\u597d\uff0c\u9002\u5408\u5199\u4ee3\u7801 \u2615\", \"categoryId\": \"notes\"}"
                                    ),
                                    @ExampleObject(
                                            name = "\u7f8e\u98df\u52a8\u6001\uff08\u5e26\u56fe\uff09",
                                            value = "{\"content\": \"\u8d85\u597d\u5403\u7684\u725b\u8089\u9762 \ud83c\udf5c\", \"mediaType\": \"image\", \"mediaUrls\": [\"https://file.hirongbao.com/xxx.jpg\"], \"categoryId\": \"food\", \"categoryName\": \"\u7f8e\u98df\"}"
                                    ),
                                    @ExampleObject(
                                            name = "\u98ce\u666f\u52a8\u6001\uff08\u591a\u56fe\uff09",
                                            value = "{\"content\": \"\u897f\u6e56\u65e5\u843d \ud83c\udf05\", \"mediaType\": \"image\", \"mediaUrls\": [\"https://file.hirongbao.com/a.jpg\", \"https://file.hirongbao.com/b.jpg\"], \"categoryId\": \"scenery\", \"categoryName\": \"\u98ce\u666f\"}"
                                    )
                            }
                    )
            )
    )
    @SecurityRequirement(name = "X-Service-Token")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/post")
    public ApiResponse<SitePost> createPost(
            @Valid @RequestBody PostUpsertRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(description = "\u901a\u8fc7\u81ea\u5b9a\u4e49 Header \u4f20\u9012\u8bbf\u95ee\u51ed\u8bc1") @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @Parameter(description = "\u901a\u8fc7\u6807\u51c6 Authorization Header \u4f20\u9012\u51ed\u8bc1 (Bearer xxx)") @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = serviceToken;
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7).trim();
        }
        ServiceToken serviceTokenEntity = tokenService.requireActive(token, HUB);
        httpRequest.setAttribute("auth.tokenName", serviceTokenEntity.getTokenName());
        tokenService.recordUsage(serviceTokenEntity, "create_post");
        return ApiResponse.success(postService.create(request));
    }


}
