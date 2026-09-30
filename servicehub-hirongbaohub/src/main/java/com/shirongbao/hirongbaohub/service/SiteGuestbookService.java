package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.hirongbaohub.entity.SiteGuestbook;
import com.shirongbao.hirongbaohub.entity.SiteUser;
import com.shirongbao.hirongbaohub.mapper.SiteGuestbookMapper;
import com.shirongbao.hirongbaohub.mapper.SiteUserMapper;
import com.shirongbao.hirongbaohub.security.UserContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class SiteGuestbookService {
    private final SiteGuestbookMapper guestbookMapper;
    private final SiteUserMapper userMapper;

    public SiteGuestbookService(SiteGuestbookMapper guestbookMapper, SiteUserMapper userMapper) {
        this.guestbookMapper = guestbookMapper;
        this.userMapper = userMapper;
    }

    public SiteGuestbook addMessage(String content, Long targetUserId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("留言内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("留言内容不能超过500字");
        }

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        QueryWrapper<SiteGuestbook> query = new QueryWrapper<SiteGuestbook>()
                .eq("user_id", userId)
                .between("created_at", startOfDay, endOfDay);
        if (targetUserId == null) {
            query.isNull("target_user_id");
        } else {
            query.eq("target_user_id", targetUserId);
        }

        Long countToday = guestbookMapper.selectCount(query);
                
        if (countToday != null && countToday > 0) {
            throw new IllegalArgumentException("您今日已留下足迹，明天再来吧~");
        }
        
        SiteGuestbook msg = new SiteGuestbook();
        msg.setUserId(userId);
        msg.setTargetUserId(targetUserId);
        msg.setContent(content.trim());
        msg.setCreatedAt(LocalDateTime.now());
        guestbookMapper.insert(msg);
        return msg;
    }

    public Page<Map<String, Object>> getMessageList(int page, int size, Long targetUserId) {
        Page<SiteGuestbook> pg = new Page<>(page, size);
        QueryWrapper<SiteGuestbook> query = new QueryWrapper<SiteGuestbook>().orderByDesc("created_at");
        if (targetUserId == null) {
            query.isNull("target_user_id");
        } else {
            query.eq("target_user_id", targetUserId);
        }
        guestbookMapper.selectPage(pg, query);
        
        List<Map<String, Object>> records = pg.getRecords().stream().map(g -> {
            SiteUser user = userMapper.selectById(g.getUserId());
            return Map.<String, Object>of(
                "id", g.getId(),
                "userId", g.getUserId(),
                "accountName", user != null ? user.getAccountName() : "未知用户",
                "avatarUrl", (user != null && user.getAvatarUrl() != null) ? user.getAvatarUrl() : "",
                "content", g.getContent(),
                "createdAt", g.getCreatedAt()
            );
        }).toList();
        
        Page<Map<String, Object>> result = new Page<>(page, size, pg.getTotal());
        result.setRecords(records);
        return result;
    }
}
