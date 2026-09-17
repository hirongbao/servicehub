/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站访客评论业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.CommentCreateRequest;
import com.shirongbao.hirongbaohub.entity.SiteComment;
import com.shirongbao.hirongbaohub.mapper.SiteCommentMapper;
import com.shirongbao.noticehub.service.NoticeService;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.entity.SitePost;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteCommentService {
    private final SiteCommentMapper mapper;
    private final NoticeService noticeService;
    private final SitePostMapper postMapper;

    // 初始化评论业务服务
    public SiteCommentService(SiteCommentMapper mapper, NoticeService noticeService, SitePostMapper postMapper) {
        this.mapper = mapper;
        this.noticeService = noticeService;
        this.postMapper = postMapper;
    }

    // 批量填充动态的评论列表（树形结构：顶级评论含 children 子回复）
    public void fillByPostIds(List<Long> postIds, Map<Long, List<SiteComment>> target) {
        if (postIds.isEmpty()) {
            return;
        }
        List<SiteComment> all = mapper.selectList(new LambdaQueryWrapper<SiteComment>()
                .in(SiteComment::getPostId, postIds)
                .eq(SiteComment::getStatus, 1) // Only approved
                .orderByAsc(SiteComment::getCreatedAt)
                .orderByAsc(SiteComment::getId));

        // 按 postId 分组后构建树
        Map<Long, List<SiteComment>> byPost = all.stream().collect(Collectors.groupingBy(SiteComment::getPostId));
        for (Map.Entry<Long, List<SiteComment>> entry : byPost.entrySet()) {
            target.put(entry.getKey(), buildTree(entry.getValue()));
        }
    }

    // 将扁平评论列表组装为树形结构
    private List<SiteComment> buildTree(List<SiteComment> flat) {
        Map<Long, SiteComment> map = new java.util.LinkedHashMap<>();
        for (SiteComment c : flat) {
            c.setChildren(new java.util.ArrayList<>());
            map.put(c.getId(), c);
        }
        List<SiteComment> roots = new java.util.ArrayList<>();
        for (SiteComment c : flat) {
            if (c.getParentId() != null && map.containsKey(c.getParentId())) {
                map.get(c.getParentId()).getChildren().add(c);
            } else {
                roots.add(c);
            }
        }
        return roots;
    }

    // 发表访客评论（重载兼顾旧签名）
    public SiteComment add(Long postId, CommentCreateRequest request) {
        return add(postId, request, null);
    }

    public SiteComment add(Long postId, CommentCreateRequest request, String ipAddress) {
        SiteComment comment = new SiteComment();
        comment.setPostId(postId);
        String authorName = request.author() == null || request.author().isBlank() ? "访客" : request.author().trim();
        comment.setAuthor(authorName);
        comment.setIpAddress(ipAddress != null && ipAddress.length() > 45 ? ipAddress.substring(0, 45) : ipAddress);
        String commentContent = request.content().trim();
        comment.setContent(commentContent);
        comment.setStatus(0); // 0: pending

        // 回复逻辑：设置 parentId 和 replyToAuthor
        if (request.parentId() != null) {
            SiteComment parent = mapper.selectById(request.parentId());
            if (parent != null && parent.getPostId().equals(postId)) {
                comment.setParentId(parent.getId());
                comment.setReplyToAuthor(parent.getAuthor());
            }
        }

        mapper.insert(comment);
        
        try {
            SitePost post = postMapper.selectById(postId);
            String postTitle = post != null && post.getContent() != null ? post.getContent() : "未知动态";
            if (postTitle.length() > 30) {
                postTitle = postTitle.substring(0, 30) + "...";
            }
            String notifyContent = comment.getReplyToAuthor() != null
                    ? "回复 @" + comment.getReplyToAuthor() + "：" + commentContent
                    : commentContent;
            noticeService.sendNewCommentNotification("hirongbao@qq.com", postTitle, authorName, notifyContent, ipAddress);
        } catch (Exception e) {
            System.err.println("发送评论审核通知失败: " + e.getMessage());
        }
        
        return comment;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.shirongbao.hirongbaohub.dto.AdminCommentResponse> adminPage(int page, int size, com.shirongbao.hirongbaohub.mapper.SitePostMapper postMapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SiteComment> pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        mapper.selectPage(pg, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteComment>()
                .orderByDesc(SiteComment::getCreatedAt));
        
        List<com.shirongbao.hirongbaohub.dto.AdminCommentResponse> list = pg.getRecords().stream().map(c -> {
            com.shirongbao.hirongbaohub.entity.SitePost post = postMapper.selectById(c.getPostId());
            String summary = "已删除或不存在的动态";
            if (post != null) {
                summary = post.getContent() != null ? post.getContent() : "（纯媒体动态）";
                if (summary.length() > 20) summary = summary.substring(0, 20) + "...";
            }
            return new com.shirongbao.hirongbaohub.dto.AdminCommentResponse(c, summary);
        }).toList();
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.shirongbao.hirongbaohub.dto.AdminCommentResponse> result = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size, pg.getTotal());
        result.setRecords(list);
        return result;
    }

    public void updateStatus(Long id, Integer status) {
        SiteComment comment = new SiteComment();
        comment.setId(id);
        comment.setStatus(status);
        mapper.updateById(comment);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
