package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.hirongbaohub.entity.SiteAnniversary;
import com.shirongbao.hirongbaohub.mapper.SiteAnniversaryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteAnniversaryService extends ServiceImpl<SiteAnniversaryMapper, SiteAnniversary> {
    
    public List<SiteAnniversary> getAllAnniversaries() {
        return lambdaQuery().orderByAsc(SiteAnniversary::getEventDate).list();
    }

    public List<SiteAnniversary> getEnabledAnniversaries() {
        return lambdaQuery().eq(SiteAnniversary::getIsEnabled, true).orderByAsc(SiteAnniversary::getEventDate).list();
    }
}
