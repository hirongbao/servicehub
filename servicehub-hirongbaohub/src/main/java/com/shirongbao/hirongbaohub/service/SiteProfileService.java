/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站站点资料业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.ProfileResponse;
import com.shirongbao.hirongbaohub.entity.SiteProfile;
import com.shirongbao.hirongbaohub.entity.SiteSocial;
import com.shirongbao.hirongbaohub.mapper.SiteProfileMapper;
import com.shirongbao.hirongbaohub.mapper.SiteSocialMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteProfileService {
    private final SiteProfileMapper profileMapper;
    private final SiteSocialMapper socialMapper;

    // 初始化站点资料服务
    public SiteProfileService(SiteProfileMapper profileMapper, SiteSocialMapper socialMapper) {
        this.profileMapper = profileMapper;
        this.socialMapper = socialMapper;
    }

    // 查询启用中的站点资料与社交名片
    public ProfileResponse getProfile() {
        SiteProfile profile = profileMapper.selectOne(new LambdaQueryWrapper<SiteProfile>()
                .eq(SiteProfile::getStatus, 1)
                .orderByAsc(SiteProfile::getId)
                .last("LIMIT 1"));
        if (profile == null) {
            throw new IllegalArgumentException("站点资料尚未配置");
        }
        List<SiteSocial> socials = socialMapper.selectList(new LambdaQueryWrapper<SiteSocial>()
                .eq(SiteSocial::getStatus, 1)
                .orderByAsc(SiteSocial::getSortOrder)
                .orderByAsc(SiteSocial::getId));
        List<ProfileResponse.SocialItem> items = socials.stream()
                .map(s -> new ProfileResponse.SocialItem(s.getPlatform(), s.getIconName(), s.getUrl(), s.getQrCodeUrl()))
                .toList();
        ProfileResponse.Stats stats = new ProfileResponse.Stats(
                profile.getStatPosts(), profile.getStatFollowers(), profile.getStatFollowing());
        return new ProfileResponse(profile.getName(), profile.getHandle(), profile.getBio(),
                profile.getAvatarUrl(), items, stats);
    }
}
