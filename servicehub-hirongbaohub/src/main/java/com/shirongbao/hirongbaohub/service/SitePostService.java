/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站动态业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SitePostService {
    private static final Set<String> MEDIA_TYPES = Set.of("image", "video");

    private final SitePostMapper mapper;

    // 初始化动态业务服务
    public SitePostService(SitePostMapper mapper) {
        this.mapper = mapper;
    }

    // 查询全部动态（管理端，按发布时间倒序）
    public List<SitePost> list() {
        return mapper.selectList(new LambdaQueryWrapper<SitePost>()
                .orderByDesc(SitePost::getCreatedAt)
                .orderByDesc(SitePost::getId));
    }

    // 发布动态
    public SitePost create(PostUpsertRequest request) {
        SitePost post = new SitePost();
        post.setContent(request.content());
        post.setLikeCount(0);
        post.setStatus(1);
        applyMedia(post, request);
        mapper.insert(post);
        return post;
    }

    // 编辑动态内容与媒体
    public SitePost update(Long id, PostUpsertRequest request) {
        SitePost post = require(id);
        post.setContent(request.content());
        applyMedia(post, request);
        mapper.updateById(post);
        return post;
    }

    // 更新动态状态（发布/下架）
    public SitePost updateStatus(Long id, Integer status) {
        SitePost post = require(id);
        post.setStatus(status == null || status != 1 ? 0 : 1);
        mapper.updateById(post);
        return post;
    }

    // 删除动态
    public void delete(Long id) {
        mapper.deleteById(require(id).getId());
    }

    // 填充媒体字段并校验组合合法性
    private void applyMedia(SitePost post, PostUpsertRequest request) {
        String mediaType = normalize(request.mediaType());
        String mediaUrl = trimToNull(request.mediaUrl());
        if (mediaUrl != null && mediaType == null) {
            throw new IllegalArgumentException("填写了媒体链接时必须选择媒体类型");
        }
        if (mediaType != null && mediaUrl == null) {
            throw new IllegalArgumentException("选择媒体类型时必须填写媒体链接");
        }
        post.setMediaType(mediaType);
        post.setMediaUrl(mediaUrl);
    }

    // 校验媒体类型取值，空白归一为 null
    private String normalize(String mediaType) {
        if (mediaType == null || mediaType.isBlank()) {
            return null;
        }
        if (!MEDIA_TYPES.contains(mediaType)) {
            throw new IllegalArgumentException("媒体类型只支持 image 或 video");
        }
        return mediaType;
    }

    // 字符串去空格，空白转为 null
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // 按 id 加载动态，不存在时抛出业务异常
    private SitePost require(Long id) {
        SitePost post = mapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("动态不存在或已删除");
        }
        return post;
    }
}
