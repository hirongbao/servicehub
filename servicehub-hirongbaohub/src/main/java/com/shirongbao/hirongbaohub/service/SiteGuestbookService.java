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

    public SiteGuestbook addMessage(String content) {
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
        
        Long countToday = guestbookMapper.selectCount(new QueryWrapper<SiteGuestbook>()
                .eq("user_id", userId)
                .between("created_at", startOfDay, endOfDay));
                
        if (countToday != null && countToday > 0) {
            throw new IllegalArgumentException("您今日已留下足迹，明天再来吧~");
        }
        
        SiteGuestbook msg = new SiteGuestbook();
        msg.setUserId(userId);
        msg.setContent(content.trim());
        msg.setCreatedAt(LocalDateTime.now());
        guestbookMapper.insert(msg);
        return msg;
    }

    public Page<Map<String, Object>> getMessageList(int page, int size) {
        Page<SiteGuestbook> pg = new Page<>(page, size);
        guestbookMapper.selectPage(pg, new QueryWrapper<SiteGuestbook>().orderByDesc("created_at"));
        
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
