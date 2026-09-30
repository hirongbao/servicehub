package com.shirongbao.hirongbaohub.controller;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.hirongbaohub.dto.SendCodeRequest;
import com.shirongbao.hirongbaohub.dto.UserLoginRequest;
import com.shirongbao.hirongbaohub.dto.UserRegisterRequest;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.security.UserContext;
import com.shirongbao.hirongbaohub.service.SiteUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class SiteUserController {

    private final SiteUserService userService;

    public SiteUserController(SiteUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        userService.sendVerificationCode(request.getEmail());
        return ApiResponse.success();
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping("/info")
    public ApiResponse<SiteUser> getInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return ApiResponse.success(userService.getUserInfo(userId));
    }
}
