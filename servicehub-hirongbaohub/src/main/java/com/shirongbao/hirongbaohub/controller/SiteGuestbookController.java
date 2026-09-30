package com.shirongbao.hirongbaohub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.service.SiteGuestbookService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/guestbook")
public class SiteGuestbookController {
    
    private final SiteGuestbookService guestbookService;

    public SiteGuestbookController(SiteGuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    public record GuestbookRequest(String content) {}

    @PostMapping("/add")
    public ApiResponse<Void> addMessage(@RequestBody GuestbookRequest req) {
        guestbookService.addMessage(req.content());
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(guestbookService.getMessageList(page, size));
    }
}
