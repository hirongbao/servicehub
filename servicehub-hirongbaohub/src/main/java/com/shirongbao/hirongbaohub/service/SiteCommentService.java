/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站访客评论业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.hirongbaohub.dto.AdminCommentResponse;
import com.shirongbao.hirongbaohub.dto.CommentCreateRequest;
import com.shirongbao.hirongbaohub.entity.SiteComment;
import com.shirongbao.hirongbaohub.entity.SitePost;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteCommentMapper;
import com.shirongbao.hirongbaohub.mapper.SitePostMapper;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteCommentService {

    private final SiteCommentMapper mapper;
    private final SiteNotificationService noticeService;
    private final SitePostMapper postMapper;
    private final SiteUserMapper userMapper;
    private final SiteUserService siteUserService;

    // 批量填充动态的评论列表（树形结构：顶级评论含 children 子回复）
    public void fillByPostIds(List<Long> postIds, Map<Long, List<SiteComment>> target) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        List<SiteComment> all = mapper.selectList(new LambdaQueryWrapper<SiteComment>()
                .in(SiteComment::getPostId, postIds)
                .eq(SiteComment::getStatus, 1)
                .orderByAsc(SiteComment::getCreatedAt)
                .orderByAsc(SiteComment::getId));

        List<Long> userIds = all.stream().map(SiteComment::getUserId).filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        List<String> authorsWithoutUserId = all.stream()
                .filter(c -> c.getUserId() == null && c.getAuthor() != null && !c.getAuthor().isBlank() && !"访客".equals(c.getAuthor()))
                .map(SiteComment::getAuthor)
                .distinct()
                .toList();

        Map<Long, SiteUser> userMap = userIds.isEmpty() ? java.util.Collections.emptyMap() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SiteUser::getId, u -> u));

        Map<String, SiteUser> authorMap = authorsWithoutUserId.isEmpty() ? java.util.Collections.emptyMap() :
                userMapper.selectList(new LambdaQueryWrapper<SiteUser>().in(SiteUser::getAccountName, authorsWithoutUserId)).stream()
                        .collect(Collectors.toMap(SiteUser::getAccountName, u -> u, (k1, k2) -> k1));

        for (SiteComment c : all) {
            if (c.getUserId() != null && userMap.containsKey(c.getUserId())) {
                SiteUser u = userMap.get(c.getUserId());
                c.setAuthorAvatar(u.getAvatarUrl());
            } else if (c.getUserId() == null && c.getAuthor() != null && authorMap.containsKey(c.getAuthor())) {
                SiteUser u = authorMap.get(c.getAuthor());
                c.setUserId(u.getId());
                c.setAuthorAvatar(u.getAvatarUrl());
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
        Map<Long, SiteComment> map = new LinkedHashMap<>();
        for (SiteComment c : flat) {
            c.setChildren(new ArrayList<>());
            map.put(c.getId(), c);
        }
        List<SiteComment> roots = new ArrayList<>();
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

    // 发表访客评论
    public SiteComment add(Long postId, CommentCreateRequest request, String ipAddress) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("必须登录才能发表评论");
        }
        SiteUser user = userMapper.selectById(userId);
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
            Long targetUserId = post.getUserId() != null ? post.getUserId() : siteUserService.getAdminUserId();

            // 如果是回复某人，通知被回复的人
            if (comment.getParentId() != null) {
                SiteComment parent = mapper.selectById(comment.getParentId());
                if (parent != null) {
                    if (parent.getUserId() != null) {
                        targetUserId = parent.getUserId();
                    } else if (parent.getAuthor() != null) {
                        SiteUser parentUser = userMapper.selectOne(new LambdaQueryWrapper<SiteUser>().eq(SiteUser::getAccountName, parent.getAuthor()));
                        if (parentUser != null) {
                            targetUserId = parentUser.getId();
                        }
                    }
                }
            }

            // 不要给自己发通知
            if (targetUserId != null && !targetUserId.equals(userId)) {
                noticeService.notify(targetUserId, "COMMENT", comment.getId(), user.getAccountName(), summary);
            }
        }

        return comment;
    }

    // 管理后台分页查询评论
    public Page<AdminCommentResponse> adminPage(int page, int size, SitePostMapper postMapper) {
        Page<SiteComment> pg = new Page<>(page, size);
        mapper.selectPage(pg, new LambdaQueryWrapper<SiteComment>()
                .orderByDesc(SiteComment::getCreatedAt));

        List<SiteComment> records = pg.getRecords();
        List<Long> userIds = records.stream().map(SiteComment::getUserId).filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            Map<Long, SiteUser> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(SiteUser::getId, u -> u));
            for (SiteComment c : records) {
                if (c.getUserId() != null && userMap.containsKey(c.getUserId())) {
                    c.setAuthorAvatar(userMap.get(c.getUserId()).getAvatarUrl());
                }
            }
        }

        List<AdminCommentResponse> list = records.stream().map(c -> {
            SitePost post = postMapper.selectById(c.getPostId());
            String summary = "已删除或不存在的动态";
            if (post != null) {
                summary = post.getContent() != null ? post.getContent() : "（纯媒体动态）";
                if (summary.length() > 20) summary = summary.substring(0, 20) + "...";
            }
            return new AdminCommentResponse(c, summary);
        }).toList();

        Page<AdminCommentResponse> result = new Page<>(page, size, pg.getTotal());
        result.setRecords(list);
        return result;
    }

    // 更新评论审核状态
    public void updateStatus(Long id, Integer status) {
        SiteComment comment = new SiteComment();
        comment.setId(id);
        comment.setStatus(status);
        mapper.updateById(comment);
    }

    // 删除评论
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
