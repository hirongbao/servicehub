/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站动态业务服务（内容与媒体至少其一，图片最多 9 张，视频 1 条）
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SitePostMedia;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.mapper.SitePostMediaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SitePostService {
    private static final Set<String> MEDIA_TYPES = Set.of("image", "video");
    private static final int MAX_IMAGES = 9;

    private final SitePostMapper mapper;
    private final SitePostMediaMapper mediaMapper;

    // 初始化动态业务服务
    public SitePostService(SitePostMapper mapper, SitePostMediaMapper mediaMapper) {
        this.mapper = mapper;
        this.mediaMapper = mediaMapper;
    }

    // 查询全部动态及其媒体列表（管理端，按发布时间倒序）
    public List<SitePost> list() {
        List<SitePost> posts = mapper.selectList(new LambdaQueryWrapper<SitePost>()
                .orderByDesc(SitePost::getCreatedAt)
                .orderByDesc(SitePost::getId));
        fillMedia(posts);
        return posts;
    }

    // 发布动态
    @Transactional
    public SitePost create(PostUpsertRequest request) {
        String content = trimToNull(request.content());
        List<String> urls = normalizeUrls(request.mediaUrls());
        String mediaType = resolveMediaType(request.mediaType(), urls);
        if (content == null && urls.isEmpty()) {
            throw new IllegalArgumentException("动态内容和媒体至少要有一个");
        }
        SitePost post = new SitePost();
        post.setContent(content);
        post.setLikeCount(0);
        post.setStatus(1);
        mapper.insert(post);
        insertMedia(post.getId(), mediaType, urls);
        post.setMedia(mediaMapper.selectList(new LambdaQueryWrapper<SitePostMedia>()
                .eq(SitePostMedia::getPostId, post.getId())
                .orderByAsc(SitePostMedia::getSortOrder)));
        return post;
    }

    // 编辑动态内容与媒体（媒体整体替换）
    @Transactional
    public SitePost update(Long id, PostUpsertRequest request) {
        SitePost post = require(id);
        String content = trimToNull(request.content());
        List<String> urls = normalizeUrls(request.mediaUrls());
        String mediaType = resolveMediaType(request.mediaType(), urls);
        if (content == null && urls.isEmpty()) {
            throw new IllegalArgumentException("动态内容和媒体至少要有一个");
        }
        post.setContent(content);
        mapper.updateById(post);
        mediaMapper.delete(new LambdaQueryWrapper<SitePostMedia>()
                .eq(SitePostMedia::getPostId, post.getId()));
        insertMedia(post.getId(), mediaType, urls);
        post.setMedia(mediaMapper.selectList(new LambdaQueryWrapper<SitePostMedia>()
                .eq(SitePostMedia::getPostId, post.getId())
                .orderByAsc(SitePostMedia::getSortOrder)));
        return post;
    }

    // 更新动态状态（发布/下架）
    public SitePost updateStatus(Long id, Integer status) {
        SitePost post = require(id);
        post.setStatus(status == null || status != 1 ? 0 : 1);
        mapper.updateById(post);
        return post;
    }

    // 删除动态及其媒体
    @Transactional
    public void delete(Long id) {
        SitePost post = require(id);
        mediaMapper.delete(new LambdaQueryWrapper<SitePostMedia>()
                .eq(SitePostMedia::getPostId, post.getId()));
        mapper.deleteById(post.getId());
    }

    // 批量填充动态的媒体列表
    private void fillMedia(List<SitePost> posts) {
        if (posts.isEmpty()) {
            return;
        }
        List<Long> postIds = posts.stream().map(SitePost::getId).toList();
        Map<Long, List<SitePostMedia>> grouped = mediaMapper.selectList(new LambdaQueryWrapper<SitePostMedia>()
                        .in(SitePostMedia::getPostId, postIds)
                        .orderByAsc(SitePostMedia::getSortOrder)
                        .orderByAsc(SitePostMedia::getId))
                .stream()
                .collect(Collectors.groupingBy(SitePostMedia::getPostId));
        for (SitePost post : posts) {
            post.setMedia(grouped.getOrDefault(post.getId(), List.of()));
        }
    }

    // 保存媒体列表，url 按提交顺序写入排序值
    private void insertMedia(Long postId, String mediaType, List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            SitePostMedia media = new SitePostMedia();
            media.setPostId(postId);
            media.setMediaType(mediaType);
            media.setMediaUrl(urls.get(i));
            media.setSortOrder(i);
            mediaMapper.insert(media);
        }
    }

    // 校验媒体类型与数量组合，返回归一化的媒体类型（纯文字返回 null）
    private String resolveMediaType(String mediaType, List<String> urls) {
        if (mediaType == null || mediaType.isBlank()) {
            if (!urls.isEmpty()) {
                throw new IllegalArgumentException("填写了媒体链接时必须选择媒体类型");
            }
            return null;
        }
        if (!MEDIA_TYPES.contains(mediaType)) {
            throw new IllegalArgumentException("媒体类型只支持 image 或 video");
        }
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("video".equals(mediaType) ? "请填写视频链接" : "请至少添加一张图片");
        }
        if ("video".equals(mediaType) && urls.size() > 1) {
            throw new IllegalArgumentException("一条动态只能包含一个视频");
        }
        if ("image".equals(mediaType) && urls.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("图片最多 " + MAX_IMAGES + " 张");
        }
        return mediaType;
    }

    // 媒体链接列表去空格、去空项
    private List<String> normalizeUrls(List<String> mediaUrls) {
        if (mediaUrls == null) {
            return List.of();
        }
        return mediaUrls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(String::trim)
                .toList();
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
