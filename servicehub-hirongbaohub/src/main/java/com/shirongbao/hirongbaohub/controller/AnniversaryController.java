package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import com.shirongbao.hirongbaohub.service.SiteUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hirongbaohub")
public class AnniversaryController {
    
    private final SiteAnniversaryService service;
    private final SiteUserMapper userMapper;
    private final SiteUserService siteUserService;
    
    public AnniversaryController(SiteAnniversaryService service, SiteUserMapper userMapper, SiteUserService siteUserService) {
        this.service = service;
        this.userMapper = userMapper;
        this.siteUserService = siteUserService;
    }
    
    @GetMapping("/anniversaries")
    public ApiResponse<List<SiteAnniversary>> getAllAnniversaries() {
        return ApiResponse.success(service.getEnabledAnniversaries(siteUserService.getAdminUserId()));
    }

    @GetMapping("/anniversaries/user/{accountName}")
    public ApiResponse<List<SiteAnniversary>> getUserAnniversaries(@PathVariable String accountName) {
        SiteUser user = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, accountName));
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(service.getEnabledAnniversariesByUserId(user.getId()));
    }
}
