package com.shirongbao.admin.controller;

import lombok.RequiredArgsConstructor;

import com.shirongbao.common.response.ApiResponse;
import com.shirongbao.filehub.entity.FileRecord;
import com.shirongbao.filehub.service.FileRecordService;
import com.shirongbao.hirongbaohub.security.UserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

@RestController
@RequestMapping("/api/posts/ugc")
@RequiredArgsConstructor
public class UserUgcFileController {

    private final FileRecordService fileRecordService;
    private final StringRedisTemplate redisTemplate;

    private void checkRateLimit() {
        String key1m = "rate_limit:upload:1m";
        String key1h = "rate_limit:upload:1h";
        String key1d = "rate_limit:upload:1d";

        Long count1m = redisTemplate.opsForValue().increment(key1m);
        if (count1m != null && count1m == 1) redisTemplate.expire(key1m, 1, TimeUnit.MINUTES);
        if (count1m != null && count1m > 10) throw new IllegalArgumentException("操作太频繁，请稍后再试 (每分钟最多上传10次)");

        Long count1h = redisTemplate.opsForValue().increment(key1h);
        if (count1h != null && count1h == 1) redisTemplate.expire(key1h, 1, TimeUnit.HOURS);
        if (count1h != null && count1h > 500) throw new IllegalArgumentException("操作太频繁，请稍后再试 (每小时最多上传500次)");

        Long count1d = redisTemplate.opsForValue().increment(key1d);
        if (count1d != null && count1d == 1) redisTemplate.expire(key1d, 1, TimeUnit.DAYS);
        if (count1d != null && count1d > 300) throw new IllegalArgumentException("操作太频繁，今日上传次数已达上限");
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<FileRecord> upload(@RequestPart("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能上传图片");
        }
        checkRateLimit();
        // UGC 上传的图片来源标记为 UGC
        return ApiResponse.success(fileRecordService.upload(file, null, "UGC", userId));
    }
}
