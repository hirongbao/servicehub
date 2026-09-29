package com.shirongbao.admin.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/anniversaries")
public class AdminAnniversaryController {

    private final SiteAnniversaryService service;

    public AdminAnniversaryController(SiteAnniversaryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SiteAnniversary>> list() {
        return ApiResponse.success(service.getAllAnniversaries());
    }

    @PostMapping
    public ApiResponse<SiteAnniversary> save(@RequestBody SiteAnniversary anniversary) {
        service.saveOrUpdate(anniversary);
        return ApiResponse.success(anniversary);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.removeById(id);
        return ApiResponse.success(null);
    }
}
