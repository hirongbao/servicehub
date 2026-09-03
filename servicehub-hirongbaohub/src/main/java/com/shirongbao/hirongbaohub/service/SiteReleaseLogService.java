/*
 * auth: hirongbao
 * create: 2026-09-03
 * desc: 个人网站更新日志业务服务
 */
package com.shirongbao.hirongbaohub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.hirongbaohub.dto.ReleaseLogUpsertRequest;
import com.shirongbao.hirongbaohub.entity.SiteReleaseLog;
import com.shirongbao.hirongbaohub.mapper.SiteReleaseLogMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteReleaseLogService {
    private final SiteReleaseLogMapper mapper;

    // 初始化更新日志服务
    public SiteReleaseLogService(SiteReleaseLogMapper mapper) { this.mapper = mapper; }

    // 查询已发布更新日志
    public List<SiteReleaseLog> published() {
        return mapper.selectList(new LambdaQueryWrapper<SiteReleaseLog>().eq(SiteReleaseLog::getStatus, 1)
                .orderByDesc(SiteReleaseLog::getPublishedAt).orderByDesc(SiteReleaseLog::getId));
    }

    // 查询全部更新日志供管理端维护
    public List<SiteReleaseLog> list() {
        return mapper.selectList(new LambdaQueryWrapper<SiteReleaseLog>().orderByDesc(SiteReleaseLog::getPublishedAt).orderByDesc(SiteReleaseLog::getId));
    }

    // 创建更新日志并立即发布
    public SiteReleaseLog create(ReleaseLogUpsertRequest request) {
        SiteReleaseLog log = new SiteReleaseLog();
        apply(log, request);
        log.setStatus(1);
        log.setPublishedAt(LocalDateTime.now());
        mapper.insert(log);
        return log;
    }

    // 编辑更新日志
    public SiteReleaseLog update(Long id, ReleaseLogUpsertRequest request) {
        SiteReleaseLog log = require(id);
        apply(log, request);
        mapper.updateById(log);
        return log;
    }

    // 切换更新日志发布状态
    public SiteReleaseLog updateStatus(Long id, Integer status) {
        SiteReleaseLog log = require(id);
        log.setStatus(status != null && status == 1 ? 1 : 0);
        if (log.getStatus() == 1 && log.getPublishedAt() == null) log.setPublishedAt(LocalDateTime.now());
        mapper.updateById(log);
        return log;
    }

    // 删除更新日志
    public void delete(Long id) { mapper.deleteById(require(id).getId()); }

    // 校验更新日志存在
    private SiteReleaseLog require(Long id) {
        SiteReleaseLog log = mapper.selectById(id);
        if (log == null) throw new IllegalArgumentException("更新日志不存在");
        return log;
    }

    // 写入更新日志字段
    private void apply(SiteReleaseLog log, ReleaseLogUpsertRequest request) {
        log.setTitle(request.title().trim());
        log.setVersion(trim(request.version()));
        log.setSummary(trim(request.summary()));
        log.setContent(trim(request.content()));
    }

    // 清理空白文本
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
