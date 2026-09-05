package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.hirongbaohub.entity.SiteSubscriber;

public interface SiteSubscriberService extends IService<SiteSubscriber> {
    void requestSubscription(String email);
    void verifySubscription(String email, String code);
    void unsubscribe(String email);
}
