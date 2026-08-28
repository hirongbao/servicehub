/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链业务服务
 */
package com.shirongbao.linkhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.linkhub.dto.LinkCreateRequest;
import com.shirongbao.linkhub.dto.LinkDailyVisit;
import com.shirongbao.linkhub.dto.LinkVisitStats;
import com.shirongbao.linkhub.entity.LinkVisitLog;
import com.shirongbao.linkhub.entity.ShortLink;
import com.shirongbao.linkhub.mapper.LinkVisitLogMapper;
import com.shirongbao.linkhub.mapper.ShortLinkMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShortLinkService {
    private static final String CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ShortLinkMapper mapper;
    private final LinkVisitLogMapper visitMapper;
    private final String baseUrl;

    // 初始化短链业务服务
    public ShortLinkService(ShortLinkMapper mapper, LinkVisitLogMapper visitMapper,
                            @Value("${servicehub.link.base-url:}") String baseUrl) {
        this.mapper = mapper;
        this.visitMapper = visitMapper;
        this.baseUrl = baseUrl;
    }

    // 查询全部短链，并附带访问统计
    public List<ShortLink> list() {
        List<ShortLink> links = mapper.selectList(null);
        if (!links.isEmpty()) {
            Map<Long, LinkVisitStats> stats = mapper.selectVisitStats().stream()
                    .collect(Collectors.toMap(LinkVisitStats::getLinkId, Function.identity()));
            for (ShortLink link : links) {
                LinkVisitStats s = stats.get(link.getId());
                if (s != null) {
                    link.setVisitCount(s.getVisitCount());
                    link.setLastVisitAt(s.getLastVisitAt());
                }
            }
        }
        return links;
    }

    // 创建短链，指定别名时校验格式与唯一性
    public ShortLink create(LinkCreateRequest request) {
        String target = request.targetUrl().trim();
        if (!target.matches("^https?://\\S{1,2000}$")) {
            throw new IllegalArgumentException("目标链接必须以 http:// 或 https:// 开头");
        }
        ShortLink link = new ShortLink();
        link.setTargetUrl(target);
        link.setRemark(StringUtils.isBlank(request.remark()) ? null : request.remark().trim());
        link.setStatus(1);
        if (request.validDays() != null && request.validDays() > 0) {
            link.setExpiresAt(LocalDateTime.now().plusDays(request.validDays()));
        }
        if (StringUtils.isBlank(request.code())) {
            insertWithRandomCode(link);
        } else {
            String code = request.code().trim();
            if (!code.matches("^[a-zA-Z0-9]{1,16}$")) {
                throw new IllegalArgumentException("自定义短码只能包含字母和数字，最长 16 位");
            }
            link.setCode(code);
            try {
                mapper.insert(link);
            } catch (DuplicateKeyException e) {
                throw new IllegalArgumentException("短码 " + code + " 已被占用");
            }
        }
        return link;
    }

    // 解析短码为有效短链，禁用或过期返回 null
    public ShortLink resolve(String code) {
        ShortLink link = mapper.selectOne(new QueryWrapper<ShortLink>().eq("code", code));
        if (link == null || link.getStatus() != 1
                || (link.getExpiresAt() != null && !link.getExpiresAt().isAfter(LocalDateTime.now()))) {
            return null;
        }
        return link;
    }

    // 记录一次短链访问
    public void recordVisit(Long linkId) {
        LinkVisitLog visit = new LinkVisitLog();
        visit.setLinkId(linkId);
        visitMapper.insert(visit);
    }

    // 更新短链状态
    public ShortLink updateStatus(Long id, Integer status) {
        ShortLink link = mapper.selectById(id);
        if (link == null) {
            throw new IllegalArgumentException("短链不存在");
        }
        link.setStatus(status == null || status == 0 ? 0 : 1);
        mapper.updateById(link);
        return link;
    }

    // 删除短链
    public void delete(Long id) {
        if (mapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("短链不存在");
        }
        visitMapper.delete(new QueryWrapper<LinkVisitLog>().eq("link_id", id));
    }

    // 查询短链的按天访问统计
    public Map<String, Object> stats(Long id) {
        ShortLink link = mapper.selectById(id);
        if (link == null) {
            throw new IllegalArgumentException("短链不存在");
        }
        Long total = visitMapper.selectCount(new QueryWrapper<LinkVisitLog>().eq("link_id", id));
        List<LinkDailyVisit> daily = visitMapper.selectDailyVisits(id, LocalDateTime.now().minusDays(30));
        return Map.of("total", total == null ? 0 : total, "daily", daily);
    }

    // 拼接短链完整地址，未配置 base-url 时按请求推断
    public String fullUrl(String code, HttpServletRequest request) {
        if (StringUtils.isNotBlank(baseUrl)) {
            return baseUrl + "/s/" + code;
        }
        String host = request.getHeader("Host");
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (StringUtils.isBlank(scheme)) {
            scheme = request.getScheme();
        }
        return scheme + "://" + host + "/s/" + code;
    }

    // 使用随机短码插入，唯一索引冲突时重试
    private void insertWithRandomCode(ShortLink link) {
        for (int i = 0; i < 3; i++) {
            link.setCode(randomCode());
            try {
                mapper.insert(link);
                return;
            } catch (DuplicateKeyException e) {
                // 短码碰撞，重试
            }
        }
        throw new IllegalStateException("短码生成失败，请稍后重试");
    }

    // 生成 6 位随机短码
    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
