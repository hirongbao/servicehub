package com.shirongbao.hirongbaohub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.hirongbaohub.entity.SiteSubscriber;
import com.shirongbao.hirongbaohub.mapper.SiteSubscriberMapper;
import com.shirongbao.hirongbaohub.service.SiteSubscriberService;
import com.shirongbao.noticehub.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SiteSubscriberServiceImpl extends ServiceImpl<SiteSubscriberMapper, SiteSubscriber> implements SiteSubscriberService {

    @Autowired
    private NoticeService noticeService;

    @Override
    public void requestSubscription(String email) {
        SiteSubscriber subscriber = this.getOne(new LambdaQueryWrapper<SiteSubscriber>().eq(SiteSubscriber::getEmail, email));
        
        String code = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);
        
        if (subscriber == null) {
            subscriber = new SiteSubscriber();
            subscriber.setEmail(email);
            subscriber.setStatus(0); // 未验证
            subscriber.setVerifyCode(code);
            subscriber.setCodeExpiresAt(expiresAt);
            subscriber.setCreatedAt(now);
            subscriber.setUpdatedAt(now);
            this.save(subscriber);
        } else {
            if (subscriber.getStatus() == 1) {
                throw new IllegalArgumentException("您已经订阅过了，无需重复订阅");
            }
            subscriber.setVerifyCode(code);
            subscriber.setCodeExpiresAt(expiresAt);
            subscriber.setUpdatedAt(now);
            subscriber.setStatus(0);
            this.updateById(subscriber);
        }
        
        noticeService.sendVerificationCode(email, code);
    }

    @Override
    public void verifySubscription(String email, String code) {
        SiteSubscriber subscriber = this.getOne(new LambdaQueryWrapper<SiteSubscriber>().eq(SiteSubscriber::getEmail, email));
        if (subscriber == null) {
            throw new IllegalArgumentException("找不到订阅记录，请先获取验证码");
        }
        if (subscriber.getStatus() == 1) {
            throw new IllegalArgumentException("该邮箱已经验证过了");
        }
        if (subscriber.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!code.equals(subscriber.getVerifyCode())) {
            throw new IllegalArgumentException("验证码错误");
        }
        
        subscriber.setStatus(1); // 已验证
        subscriber.setUpdatedAt(LocalDateTime.now());
        this.updateById(subscriber);
    }

    @Override
    public String generateUnsubscribeToken(String email) {
        SiteSubscriber subscriber = this.getOne(new LambdaQueryWrapper<SiteSubscriber>().eq(SiteSubscriber::getEmail, email));
        if (subscriber == null) return null;
        // Simple MD5 signature for token
        String raw = email + "-" + subscriber.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) + "-hirongbaohub";
        return org.springframework.util.DigestUtils.md5DigestAsHex(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public void unsubscribe(String email, String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("无效的退订链接");
        }
        String expectedToken = generateUnsubscribeToken(email);
        if (!token.equals(expectedToken)) {
            throw new IllegalArgumentException("退订链接已失效或不正确");
        }
        SiteSubscriber subscriber = this.getOne(new LambdaQueryWrapper<SiteSubscriber>().eq(SiteSubscriber::getEmail, email));
        if (subscriber != null) {
            subscriber.setStatus(2); // 已退订
            subscriber.setUpdatedAt(LocalDateTime.now());
            this.updateById(subscriber);
        }
    }
}
