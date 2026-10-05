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

import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.entity.SitePost;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteCommentService {
    private final SiteCommentMapper mapper;
    private final SiteNotificationService noticeService;
    private final SitePostMapper postMapper;
    private final com.shirongbao.hirongbaohub.mapper.SiteUserMapper userMapper;

    // 初始化评论业务服务
    public SiteCommentService(SiteCommentMapper mapper, SiteNotificationService noticeService, SitePostMapper postMapper, com.shirongbao.hirongbaohub.mapper.SiteUserMapper userMapper) {
        this.mapper = mapper;
        this.noticeService = noticeService;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    // 批量填充动态的评论列表（树形结构：顶级评论含 children 子回复）
    public void fillByPostIds(List<Long> postIds, Map<Long, List<SiteComment>> target) {
        if (postIds.isEmpty()) {
            return;
        }
        List<SiteComment> all = mapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteComment>()
                .in(SiteComment::getPostId, postIds)
                .eq(SiteComment::getStatus, 1) // Only approved
                .orderByAsc(SiteComment::getCreatedAt)
                .orderByAsc(SiteComment::getId));

        List<Long> userIds = all.stream().map(SiteComment::getUserId).filter(id -> id != null).distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            java.util.Map<Long, String> avatarMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(com.shirongbao.hirongbaohub.entity.SiteUser::getId, com.shirongbao.hirongbaohub.entity.SiteUser::getAvatarUrl));
            for (SiteComment c : all) {
                if (c.getUserId() != null && avatarMap.containsKey(c.getUserId())) {
                    c.setAuthorAvatar(avatarMap.get(c.getUserId()));
                }
            }
        }

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
        Long userId = com.shirongbao.hirongbaohub.security.UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能发表评论");
        }
        com.shirongbao.hirongbaohub.entity.SiteUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("无效的用户状态");
        }

        SiteComment comment = new SiteComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setAuthor(user.getAccountName());
        comment.setAuthorAvatar(user.getAvatarUrl());
        comment.setIpAddress(ipAddress != null && ipAddress.length() > 45 ? ipAddress.substring(0, 45) : ipAddress);
        String commentContent = request.content().trim();
        comment.setContent(commentContent);
        comment.setStatus(1); // 登录用户免审核，直接通过

        // 回复逻辑：设置 parentId 和 replyToAuthor
        if (request.parentId() != null) {
            SiteComment parent = mapper.selectById(request.parentId());
            if (parent != null && parent.getPostId().equals(postId)) {
                comment.setParentId(parent.getId());
                comment.setReplyToAuthor(parent.getAuthor());
            }
        }

        mapper.insert(comment);
        
        // 发送评论通知
        SitePost post = postMapper.selectById(postId);
        if (post != null) {
            String summary = commentContent.length() > 20 ? commentContent.substring(0, 20) + "..." : commentContent;
            Long targetUserId = post.getUserId();
            
            // 如果是回复某人，且那人存在，通知被回复的人
            if (comment.getParentId() != null && targetUserId != null) {
                SiteComment parent = mapper.selectById(comment.getParentId());
                if (parent != null && parent.getUserId() != null) {
                    targetUserId = parent.getUserId();
                }
            }
            
            // 不要给自己发通知
            if (targetUserId != null && !targetUserId.equals(userId)) {
                noticeService.notify(targetUserId, "COMMENT", comment.getId(), user.getAccountName(), summary);
            }
        }
        
        return comment;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.shirongbao.hirongbaohub.dto.AdminCommentResponse> adminPage(int page, int size, com.shirongbao.hirongbaohub.mapper.SitePostMapper postMapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SiteComment> pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        mapper.selectPage(pg, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteComment>()
                .orderByDesc(SiteComment::getCreatedAt));
        
        List<SiteComment> records = pg.getRecords();
        List<Long> userIds = records.stream().map(SiteComment::getUserId).filter(id -> id != null).distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            java.util.Map<Long, String> avatarMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(com.shirongbao.hirongbaohub.entity.SiteUser::getId, com.shirongbao.hirongbaohub.entity.SiteUser::getAvatarUrl));
            for (SiteComment c : records) {
                if (c.getUserId() != null && avatarMap.containsKey(c.getUserId())) {
                    c.setAuthorAvatar(avatarMap.get(c.getUserId()));
                }
            }
        }
        
        List<com.shirongbao.hirongbaohub.dto.AdminCommentResponse> list = records.stream().map(c -> {
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
