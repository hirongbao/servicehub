package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.service.SiteAnniversaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hirongbaohub")
public class AnniversaryController {
    
    private final SiteAnniversaryService service;
    
    public AnniversaryController(SiteAnniversaryService service) {
        this.service = service;
    }
    
    @GetMapping("/anniversaries")
    public ApiResponse<List<SiteAnniversary>> getAllAnniversaries() {
        return ApiResponse.success(service.getEnabledAnniversaries());
    }
}
