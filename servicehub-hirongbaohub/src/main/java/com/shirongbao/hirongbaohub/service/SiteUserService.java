package com.shirongbao.hirongbaohub.service;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.hirongbaohub.dto.UserLoginRequest;
import com.shirongbao.hirongbaohub.dto.UserRegisterRequest;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserCredentialService;
import com.shirongbao.noticehub.service.NoticeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteUserService {
    private final SiteUserMapper mapper;
    private final NoticeService noticeService;
    private final StringRedisTemplate redisTemplate;
    private final UserCredentialService credentialService;
    
    @Value("${servicehub.admin.username}")
    private String adminUsername;
    
    @Value("${servicehub.admin.password}")
    private String adminPassword;
    
    @Value("${servicehub.admin.email:admin@hirongbao.com}")
    private String adminEmail;

    private static final Set<String> RESERVED_WORDS = Set.of(
        "admin", "api", "root", "system", "post", "hirongbao", "user", 
        "login", "register", "auth", "test", "webmaster"
    );

    private volatile Long cachedAdminId;

    /**
     * 获取站长（ADMIN）的 user_id，带内存缓存。
     * 全局唯一入口，消除散落各处的 ADMIN 查询。
     */
    public Long getAdminUserId() {
        if (cachedAdminId == null) {
            SiteUser admin = mapper.selectOne(new QueryWrapper<SiteUser>().eq("role", "ADMIN").last("LIMIT 1"));
            if (admin != null) {
                cachedAdminId = admin.getId();
            }
        }
        return cachedAdminId;
    }

    @PostConstruct
    public void initAdmin() {
        SiteUser admin = mapper.selectOne(new QueryWrapper<SiteUser>().eq("account_name", adminUsername));
        if (admin == null) {
            admin = new SiteUser();
            admin.setAccountName(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(md5(adminPassword));
            admin.setRole("ADMIN");
            admin.setStatus(1);
            mapper.insert(admin);
        } else if (!"ADMIN".equals(admin.getRole())) {
            admin.setRole("ADMIN");
            mapper.updateById(admin);
        }
    }

    public void sendVerificationCode(String email) {
        String key = "code:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (expire != null && 300 - expire < 60) {
                throw new IllegalArgumentException("验证码发送太频繁，请稍后再试");
            }
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        noticeService.sendVerificationCode(email, code);
    }

    public void register(UserRegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码不一致");
        }
        if (RESERVED_WORDS.contains(req.getAccountName().toLowerCase())) {
            throw new IllegalArgumentException("账号名包含系统保留字，请更换");
        }
        
        String key = "code:" + req.getEmail();
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null || !cachedCode.equals(req.getCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        
        if (mapper.selectCount(new QueryWrapper<SiteUser>().eq("account_name", req.getAccountName())) > 0) {
            throw new IllegalArgumentException("账号名已被注册");
        }
        if (mapper.selectCount(new QueryWrapper<SiteUser>().eq("email", req.getEmail())) > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        
        SiteUser user = new SiteUser();
        user.setAccountName(req.getAccountName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(md5(req.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        mapper.insert(user);
        
        redisTemplate.delete(key);
    }

    public Map<String, Object> login(UserLoginRequest req) {
        QueryWrapper<SiteUser> query = new QueryWrapper<>();
        if (req.getAccount().contains("@")) {
            query.eq("email", req.getAccount());
        } else {
            query.eq("account_name", req.getAccount());
        }
        SiteUser user = mapper.selectOne(query);
        if (user == null || !user.getPasswordHash().equals(md5(req.getPassword()))) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被封禁");
        }
        
        String token = credentialService.issue(user.getId(), user.getRole());
        return Map.of(
            "token", token,
            "accountName", user.getAccountName(),
            "role", user.getRole(),
            "avatarUrl", user.getAvatarUrl() == null ? "" : user.getAvatarUrl()
        );
    }

    
    public java.util.List<java.util.Map<String, Object>> getPublicUsers() {
        return mapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteUser>()
                .eq(SiteUser::getStatus, 1)
                .orderByAsc(SiteUser::getCreatedAt))
                .stream()
                .map(u -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", u.getId());
                    map.put("accountName", u.getAccountName());
                    map.put("nickname", u.getNickname());
                    map.put("avatarUrl", u.getAvatarUrl());
                    map.put("bio", u.getBio());
                    return map;
                }).collect(java.util.stream.Collectors.toList());
    }

    public SiteUser getUserInfo(Long userId) {
        SiteUser user = mapper.selectById(userId);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }

    private String md5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
    }
}
