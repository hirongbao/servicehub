/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站站点资料业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.ProfileResponse;
import com.shirongbao.hirongbaohub.dto.ProfileUpdateRequest;
import com.shirongbao.hirongbaohub.dto.SocialUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SiteProfile;
import com.shirongbao.hirongbaohub.entity.SiteSocial;
import com.shirongbao.hirongbaohub.mapper.SiteProfileMapper;
import com.shirongbao.hirongbaohub.mapper.SiteSocialMapper;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.entity.SitePost;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteProfileService {
    private final SiteProfileMapper profileMapper;
    private final SiteSocialMapper socialMapper;
    private final SitePostMapper postMapper;

    // 初始化站点资料服务
    public SiteProfileService(SiteProfileMapper profileMapper, SiteSocialMapper socialMapper, SitePostMapper postMapper) {
        this.profileMapper = profileMapper;
        this.socialMapper = socialMapper;
        this.postMapper = postMapper;
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
        long publishedPosts = postMapper.selectCount(new LambdaQueryWrapper<SitePost>().eq(SitePost::getStatus, 1));
        ProfileResponse.Stats stats = new ProfileResponse.Stats(
                (int) publishedPosts, profile.getStatFollowers(), profile.getStatFollowing());
        return new ProfileResponse(profile.getName(), profile.getHandle(), profile.getBio(),
                profile.getAvatarUrl(), items, stats);
    }

    // 查询站点资料与全部社媒名片（管理端，含已禁用）
    public SiteProfile adminProfile() {
        SiteProfile profile = profileMapper.selectOne(new LambdaQueryWrapper<SiteProfile>()
                .orderByAsc(SiteProfile::getId)
                .last("LIMIT 1"));
        if (profile == null) {
            throw new IllegalArgumentException("站点资料尚未配置");
        }
        return profile;
    }

    // 查询全部社媒名片（管理端，按排序值与 id 升序）
    public List<SiteSocial> adminSocials() {
        return socialMapper.selectList(new LambdaQueryWrapper<SiteSocial>()
                .orderByAsc(SiteSocial::getSortOrder)
                .orderByAsc(SiteSocial::getId));
    }

    // 更新站点资料（统计数字不开放编辑，保留库中现值）
    public SiteProfile updateProfile(ProfileUpdateRequest request) {
        SiteProfile profile = adminProfile();
        profile.setName(request.name().trim());
        profile.setHandle(request.handle().trim());
        profile.setBio(trimToNull(request.bio()));
        profile.setAvatarUrl(trimToNull(request.avatarUrl()));
        profileMapper.updateById(profile);
        return profile;
    }

    // 新增社媒名片
    public SiteSocial createSocial(SocialUpsertRequest request) {
        SiteSocial social = new SiteSocial();
        applySocial(social, request);
        if (social.getStatus() == null) {
            social.setStatus(1);
        }
        socialMapper.insert(social);
        return social;
    }

    // 更新社媒名片
    public SiteSocial updateSocial(Long id, SocialUpsertRequest request) {
        SiteSocial social = socialMapper.selectById(id);
        if (social == null) {
            throw new IllegalArgumentException("社媒名片不存在");
        }
        applySocial(social, request);
        socialMapper.updateById(social);
        return social;
    }

    // 删除社媒名片
    public void deleteSocial(Long id) {
        socialMapper.deleteById(id);
    }

    // 填充社媒名片字段并校验二维码与链接至少其一
    private void applySocial(SiteSocial social, SocialUpsertRequest request) {
        String url = trimToNull(request.url());
        String qrCodeUrl = trimToNull(request.qrCodeUrl());
        if (url == null && qrCodeUrl == null) {
            throw new IllegalArgumentException("跳转链接和二维码图片至少填写一个");
        }
        social.setPlatform(request.platform().trim());
        social.setIconName(request.iconName().trim());
        social.setUrl(url);
        social.setQrCodeUrl(qrCodeUrl);
        social.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        social.setStatus(request.status() != null && request.status() == 1 ? 1 : 0);
    }

    // 字符串去空格，空白转为 null
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
