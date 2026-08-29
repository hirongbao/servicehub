/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链业务服务
 */
package com.shirongbao.linkhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shirongbao.linkhub.dto.LinkCreateRequest;
import com.shirongbao.linkhub.dto.LinkDailyVisit;
import com.shirongbao.linkhub.dto.LinkLabelVisit;
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

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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

    // 统计启用且未过期的短链数量
    public long countActive() {
        return mapper.selectCount(new QueryWrapper<ShortLink>()
                .eq("status", 1)
                .and(w -> w.isNull("expires_at").or().gt("expires_at", LocalDateTime.now())));
    }

    // 统计短链总数
    public long countAll() {
        return mapper.selectCount(null);
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

    // 记录一次短链访问，保留来源和设备信息
    public void recordVisit(Long linkId, String referer, String userAgent) {
        LinkVisitLog visit = new LinkVisitLog();
        visit.setLinkId(linkId);
        visit.setReferer(truncate(referer, 512));
        visit.setUserAgent(truncate(userAgent, 512));
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

    // 查询短链的按天、来源和设备访问统计
    public Map<String, Object> stats(Long id) {
        ShortLink link = mapper.selectById(id);
        if (link == null) {
            throw new IllegalArgumentException("短链不存在");
        }
        Long total = visitMapper.selectCount(new QueryWrapper<LinkVisitLog>().eq("link_id", id));
        List<LinkDailyVisit> daily = visitMapper.selectDailyVisits(id, LocalDateTime.now().minusDays(30));
        return Map.of("total", total == null ? 0 : total,
                "daily", daily,
                "sources", buildSources(total == null ? 0 : total, visitMapper.selectRefererVisits(id)),
                "devices", sortDevices(visitMapper.selectDeviceVisits(id)));
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

    // 按引用域名聚合访问来源并补充直接访问，按次数倒序取前十
    private List<LinkLabelVisit> buildSources(long total, List<LinkLabelVisit> referers) {
        Map<String, Long> hosts = new LinkedHashMap<>();
        long withReferer = 0;
        for (LinkLabelVisit r : referers) {
            long visits = r.getVisits() == null ? 0 : r.getVisits();
            withReferer += visits;
            hosts.merge(hostOf(r.getLabel()), visits, Long::sum);
        }
        List<LinkLabelVisit> sources = hosts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> labelVisit(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        if (total > withReferer) {
            sources.add(labelVisit("直接访问", total - withReferer));
        }
        sources.sort((a, b) -> Long.compare(b.getVisits(), a.getVisits()));
        return sources.stream().limit(10).toList();
    }

    // 从引用地址中提取展示用的域名
    private String hostOf(String referer) {
        try {
            String host = URI.create(referer).getHost();
            if (host != null) {
                return host;
            }
        } catch (IllegalArgumentException ignored) {
            // 非法引用地址，按原文展示
        }
        return referer.length() > 50 ? referer.substring(0, 50) : referer;
    }

    // 设备维度按访问次数倒序排列
    private List<LinkLabelVisit> sortDevices(List<LinkLabelVisit> devices) {
        return devices.stream()
                .peek(d -> {
                    if (d.getVisits() == null) d.setVisits(0L);
                })
                .sorted((a, b) -> Long.compare(b.getVisits(), a.getVisits()))
                .toList();
    }

    // 构造一条来源或设备统计
    private LinkLabelVisit labelVisit(String label, long visits) {
        LinkLabelVisit visit = new LinkLabelVisit();
        visit.setLabel(label);
        visit.setVisits(visits);
        return visit;
    }

    // 截断字符串到指定长度，空白返回 null
    private String truncate(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
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
