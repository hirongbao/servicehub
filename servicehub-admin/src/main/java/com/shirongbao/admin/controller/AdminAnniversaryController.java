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
    private final com.shirongbao.hirongbaohub.mapper.SiteUserMapper userMapper;

    public AdminAnniversaryController(SiteAnniversaryService service, com.shirongbao.hirongbaohub.mapper.SiteUserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
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
            com.shirongbao.hirongbaohub.entity.SiteUser adminUser = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.shirongbao.hirongbaohub.entity.SiteUser>().eq(com.shirongbao.hirongbaohub.entity.SiteUser::getRole, "ADMIN").last("LIMIT 1"));
            if (adminUser != null) {
                anniversary.setUserId(adminUser.getId());
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
