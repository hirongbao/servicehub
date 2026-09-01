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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteCommentService {
    private final SiteCommentMapper mapper;

    // 初始化评论业务服务
    public SiteCommentService(SiteCommentMapper mapper) {
        this.mapper = mapper;
    }

    // 批量填充动态的评论列表（按时间正序）
    public void fillByPostIds(List<Long> postIds, Map<Long, List<SiteComment>> target) {
        if (postIds.isEmpty()) {
            return;
        }
        Map<Long, List<SiteComment>> grouped = mapper.selectList(new LambdaQueryWrapper<SiteComment>()
                        .in(SiteComment::getPostId, postIds)
                        .orderByAsc(SiteComment::getCreatedAt)
                        .orderByAsc(SiteComment::getId))
                .stream()
                .collect(Collectors.groupingBy(SiteComment::getPostId));
        target.putAll(grouped);
    }

    // 发表访客评论
    public SiteComment add(Long postId, CommentCreateRequest request) {
        SiteComment comment = new SiteComment();
        comment.setPostId(postId);
        comment.setAuthor(request.author() == null || request.author().isBlank() ? "访客" : request.author().trim());
        comment.setContent(request.content().trim());
        mapper.insert(comment);
        return comment;
    }
}
