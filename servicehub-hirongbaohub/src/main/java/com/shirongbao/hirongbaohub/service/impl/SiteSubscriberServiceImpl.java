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
                throw new RuntimeException("该邮箱已经订阅，无需重复订阅");
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
            throw new RuntimeException("找不到订阅记录");
        }
        if (subscriber.getStatus() == 1) {
            throw new RuntimeException("该邮箱已经验证过了");
        }
        if (subscriber.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!code.equals(subscriber.getVerifyCode())) {
            throw new RuntimeException("验证码错误");
        }
        
        subscriber.setStatus(1); // 已验证
        subscriber.setUpdatedAt(LocalDateTime.now());
        this.updateById(subscriber);
    }

    @Override
    public void unsubscribe(String email) {
        SiteSubscriber subscriber = this.getOne(new LambdaQueryWrapper<SiteSubscriber>().eq(SiteSubscriber::getEmail, email));
        if (subscriber != null) {
            subscriber.setStatus(2); // 已退订
            subscriber.setUpdatedAt(LocalDateTime.now());
            this.updateById(subscriber);
        }
    }
}
