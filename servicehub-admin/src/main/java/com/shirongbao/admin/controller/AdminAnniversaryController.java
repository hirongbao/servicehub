package com.shirongbao.admin.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import com.shirongbao.hirongbaohub.service.SiteUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/anniversaries")
public class AdminAnniversaryController {

    private final SiteAnniversaryService service;
    private final SiteUserService siteUserService;

    public AdminAnniversaryController(SiteAnniversaryService service, SiteUserService siteUserService) {
        this.service = service;
        this.siteUserService = siteUserService;
    }

    @GetMapping
    public ApiResponse<List<SiteAnniversary>> list() {
        return ApiResponse.success(service.getAllAnniversaries());
    }

    @PostMapping
    public ApiResponse<SiteAnniversary> save(@RequestBody SiteAnniversary anniversary) {
        if (anniversary.getSortOrder() == null) {
            anniversary.setSortOrder(0);
        }
        if (anniversary.getUserId() == null) {
            Long adminId = siteUserService.getAdminUserId();
            if (adminId != null) {
                anniversary.setUserId(adminId);
            }
        }
        service.saveOrUpdate(anniversary);
        return ApiResponse.success(anniversary);
    }

    @PostMapping("/sort")
    public ApiResponse<Void> updateSort(@RequestBody List<SiteAnniversary> list) {
        service.updateBatchById(list);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.removeById(id);
        return ApiResponse.success(null);
    }
}
