/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 服务 Token 使用日志数据访问接口
 */
package com.shirongbao.authhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.authhub.dto.TokenUsageStats;
import com.shirongbao.authhub.entity.TokenUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TokenUsageLogMapper extends BaseMapper<TokenUsageLog> {

    // 按 Token 聚合使用次数和最近使用时间
    @Select("SELECT token_id AS tokenId, COUNT(*) AS usageCount, MAX(created_at) AS lastUsedAt "
            + "FROM token_usage_log GROUP BY token_id")
    List<TokenUsageStats> selectUsageStats();
}
