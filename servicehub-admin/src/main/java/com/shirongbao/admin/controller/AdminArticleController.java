package com.shirongbao.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteArticle;
import com.shirongbao.hirongbaohub.service.SiteArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {
    private final SiteArticleService service;

    public AdminArticleController(SiteArticleService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<IPage<SiteArticle>> list(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.success(service.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<SiteArticle> get(@PathVariable Long id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping
    public ApiResponse<SiteArticle> save(@RequestBody SiteArticle article) {
        return ApiResponse.success(service.save(article));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
