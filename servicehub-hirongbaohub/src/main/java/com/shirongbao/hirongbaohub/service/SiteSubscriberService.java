package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.hirongbaohub.entity.SiteSubscriber;

public interface SiteSubscriberService extends IService<SiteSubscriber> {
    void requestSubscription(String email);
    void verifySubscription(String email, String code);
    String generateUnsubscribeToken(String email);
    void unsubscribe(String email, String token);
}
