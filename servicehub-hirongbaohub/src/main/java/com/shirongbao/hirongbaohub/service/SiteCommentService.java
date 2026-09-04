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
                        .eq(SiteComment::getStatus, 1) // Only approved
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
        comment.setStatus(0); // 0: pending
        mapper.insert(comment);
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
