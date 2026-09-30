package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import com.shirongbao.hirongbaohub.security.UserContext;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/anniversaries/ugc")
public class UgcAnniversaryController {

    private final SiteAnniversaryService service;

    public UgcAnniversaryController(SiteAnniversaryService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ApiResponse<List<SiteAnniversary>> list() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.error("未登录");
        return ApiResponse.success(service.list(new LambdaQueryWrapper<SiteAnniversary>()
                .eq(SiteAnniversary::getUserId, userId)
                .orderByAsc(SiteAnniversary::getSortOrder)
                .orderByAsc(SiteAnniversary::getEventDate)));
    }

    @PostMapping("/save")
    public ApiResponse<SiteAnniversary> save(@RequestBody SiteAnniversary ann) {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.error("未登录");
        ann.setUserId(userId);
        if (ann.getId() == null || ann.getId().isEmpty()) {
            ann.setCreatedAt(LocalDateTime.now());
            ann.setIsEnabled(true);
            if (ann.getSortOrder() == null) ann.setSortOrder(0);
            service.save(ann);
        } else {
            SiteAnniversary exist = service.getById(ann.getId());
            if (exist == null || !exist.getUserId().equals(userId)) {
                return ApiResponse.error("无权限");
            }
            ann.setUpdatedAt(LocalDateTime.now());
            service.updateById(ann);
        }
        return ApiResponse.success(ann);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        Long userId = UserContext.getUserId();
        if (userId == null) return ApiResponse.error("未登录");
        SiteAnniversary exist = service.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return ApiResponse.error("无权限");
        }
        service.removeById(id);
        return ApiResponse.success();
    }
}
