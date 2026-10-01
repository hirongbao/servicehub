package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.mapper.SiteAnniversaryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteAnniversaryService extends ServiceImpl<SiteAnniversaryMapper, SiteAnniversary> {
    
    public List<SiteAnniversary> getAllAnniversaries() {
        return lambdaQuery().orderByAsc(SiteAnniversary::getSortOrder).orderByAsc(SiteAnniversary::getEventDate).list();
    }

    public List<SiteAnniversary> getEnabledAnniversaries(Long adminUserId) {
        return lambdaQuery().eq(SiteAnniversary::getIsEnabled, true)
                .eq(SiteAnniversary::getUserId, adminUserId)
                .orderByAsc(SiteAnniversary::getSortOrder).orderByAsc(SiteAnniversary::getEventDate).list();
    }

    public List<SiteAnniversary> getEnabledAnniversariesByUserId(Long userId) {
        return lambdaQuery().eq(SiteAnniversary::getIsEnabled, true)
                .eq(SiteAnniversary::getUserId, userId)
                .orderByAsc(SiteAnniversary::getSortOrder).orderByAsc(SiteAnniversary::getEventDate).list();
    }
}
