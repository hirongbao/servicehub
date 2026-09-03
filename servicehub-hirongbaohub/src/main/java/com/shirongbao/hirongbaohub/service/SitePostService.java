/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站动态业务服务（内容与媒体至少其一，图片最多 9 张，视频 1 条）
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.hirongbaohub.dto.CommentCreateRequest;
import com.shirongbao.hirongbaohub.dto.PostUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SiteComment;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SitePostMedia;
import com.shirongbao.hirongbaohub.entity.SiteVisitor;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.mapper.SitePostMediaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SitePostService {
    private static final Set<String> MEDIA_TYPES = Set.of("image", "video");
    private static final int MAX_IMAGES = 9;
    private static final long HEARTBEAT_TTL_MILLIS = 60_000L;
    private static final long VISIT_DEDUP_MILLIS = 1_800_000L;

    private final SitePostMapper mapper;
    private final SitePostMediaMapper mediaMapper;
    private final SiteCommentService commentService;
    private final com.shirongbao.hirongbaohub.mapper.SiteVisitorMapper visitorMapper;
    private final ConcurrentHashMap<String, Long> heartbeats = new ConcurrentHashMap<>();

    // 初始化动态业务服务
    public SitePostService(SitePostMapper mapper, SitePostMediaMapper mediaMapper, SiteCommentService commentService,
                           com.shirongbao.hirongbaohub.mapper.SiteVisitorMapper visitorMapper) {
        this.mapper = mapper;
        this.mediaMapper = mediaMapper;
        this.commentService = commentService;
        this.visitorMapper = visitorMapper;
    }

    // 查询全部动态及其媒体列表（管理端，按发布时间倒序）
    public List<SitePost> list() {
        List<SitePost> posts = mapper.selectList(new LambdaQueryWrapper<SitePost>()
                .orderByDesc(SitePost::getCreatedAt)
                .orderByDesc(SitePost::getId));
        fillMedia(posts);
        return posts;
    }

    // 查询已发布动态及媒体、评论（个人网站公开接口）
    public List<SitePost> publishedList(String category) {
        LambdaQueryWrapper<SitePost> query = new LambdaQueryWrapper<SitePost>()
                .eq(SitePost::getStatus, 1)
                .orderByDesc(SitePost::getCreatedAt)
                .orderByDesc(SitePost::getId);
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            query.eq(SitePost::getCategoryId, category.trim());
        }
        List<SitePost> posts = mapper.selectList(query);
        fillMedia(posts);
        Map<Long, List<SiteComment>> grouped = new HashMap<>();
        commentService.fillByPostIds(posts.stream().map(SitePost::getId).toList(), grouped);
        for (SitePost post : posts) {
            post.setComments(grouped.getOrDefault(post.getId(), List.of()));
            post.setCategory(toCategory(post));
        }
        return posts;
    }

    // 查询已发布动态分页数据
    public com.shirongbao.hirongbaohub.dto.PostPageResponse publishedPage(String category, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 30);
        LambdaQueryWrapper<SitePost> query = new LambdaQueryWrapper<SitePost>()
                .eq(SitePost::getStatus, 1)
                .orderByDesc(SitePost::getCreatedAt)
                .orderByDesc(SitePost::getId);
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            query.eq(SitePost::getCategoryId, category.trim());
        }
        Page<SitePost> result = mapper.selectPage(new Page<>(safePage, safeSize), query);
        List<SitePost> posts = result.getRecords();
        fillMedia(posts);
        Map<Long, List<SiteComment>> grouped = new HashMap<>();
        commentService.fillByPostIds(posts.stream().map(SitePost::getId).toList(), grouped);
        for (SitePost post : posts) {
            post.setComments(grouped.getOrDefault(post.getId(), List.of()));
            post.setCategory(toCategory(post));
        }
        return new com.shirongbao.hirongbaohub.dto.PostPageResponse(posts, safePage, safeSize,
                result.getTotal(), result.getCurrent() < result.getPages());
    }

    // 记录访客心跳并统计最近一分钟内的独立客户端
    public Map<String, Object> heartbeat(String clientId, String remoteAddress) {
        long now = System.currentTimeMillis();
        heartbeats.put(clientId.trim(), now);
        heartbeats.entrySet().removeIf(entry -> now - entry.getValue() > HEARTBEAT_TTL_MILLIS);
        recordVisit(remoteAddress);
        int actual = heartbeats.size();
        long totalVisitors = visitorMapper.selectCount(null);
        int display = displayOnlineCount(actual, totalVisitors);
        return Map.of("onlineCount", display, "actualOnlineCount", actual, "totalVisitors", totalVisitors);
    }

    // 记录独立访客并对短时间内重复访问去重
    private void recordVisit(String remoteAddress) {
        String key = sha256(remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        SiteVisitor visitor = visitorMapper.selectById(key);
        if (visitor == null) {
            visitor = new SiteVisitor();
            visitor.setVisitorKey(key);
            visitor.setFirstSeen(now);
            visitor.setLastSeen(now);
            visitor.setLastVisitAt(now);
            visitor.setVisitCount(1);
            visitorMapper.insert(visitor);
            return;
        }
        visitor.setLastSeen(now);
        if (visitor.getLastVisitAt() == null || java.time.Duration.between(visitor.getLastVisitAt(), now).toMillis() >= VISIT_DEDUP_MILLIS) {
            visitor.setLastVisitAt(now);
            visitor.setVisitCount((visitor.getVisitCount() == null ? 0 : visitor.getVisitCount()) + 1);
        }
        visitorMapper.updateById(visitor);
    }

    // 生成约三千人的展示在线数，并为每次心跳加入受控抖动
    private int displayOnlineCount(int actual, long totalVisitors) {
        int activityOffset = Math.min(Math.max(actual, 0), 100);
        int jitter = ThreadLocalRandom.current().nextInt(-80, 81);
        return Math.max(2_800, Math.min(3_200, 3_000 + activityOffset + jitter));
    }

    // 对访客地址做不可逆摘要，避免持久化原始 IP
    private String sha256(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte item : digest) result.append(String.format("%02x", item));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    // 点赞或取消点赞，返回最新点赞数
    public int like(Long id, String action) {
        SitePost post = require(id);
        int current = post.getLikeCount() == null ? 0 : post.getLikeCount();
        int next = "unlike".equals(action) ? Math.max(current - 1, 0) : current + 1;
        post.setLikeCount(next);
        mapper.updateById(post);
        return next;
    }

    // 校验动态存在后发表访客评论
    public SiteComment addComment(Long id, CommentCreateRequest request) {
        require(id);
        return commentService.add(id, request);
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
        applyCategory(post, request.categoryId(), request.categoryName());
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
        applyCategory(post, request.categoryId(), request.categoryName());
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

    // 将分类字段转换为公开接口对象
    private SitePost.Category toCategory(SitePost post) {
        String id = post.getCategoryId();
        String name = post.getCategoryName();
        if (id == null || id.isBlank()) {
            id = "notes";
        }
        if (name == null || name.isBlank()) {
            name = categoryName(id);
        }
        return new SitePost.Category(id, name);
    }

    // 写入分类标识与展示名称
    private void applyCategory(SitePost post, String categoryId, String categoryName) {
        String id = trimToNull(categoryId);
        if (id == null || "all".equalsIgnoreCase(id)) {
            id = "notes";
        }
        post.setCategoryId(id);
        post.setCategoryName(trimToNull(categoryName) == null ? categoryName(id) : categoryName.trim());
    }

    // 返回内置分类名称
    private String categoryName(String categoryId) {
        return switch (categoryId) {
            case "food" -> "美食";
            case "scenery" -> "风景";
            default -> "随笔";
        };
    }
}
