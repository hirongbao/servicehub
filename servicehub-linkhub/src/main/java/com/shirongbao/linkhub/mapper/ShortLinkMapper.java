/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链数据访问接口
 */
package com.shirongbao.linkhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.linkhub.dto.LinkVisitStats;
import com.shirongbao.linkhub.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    // 按短链聚合访问次数和最近访问时间
    @Select("SELECT link_id AS linkId, COUNT(*) AS visitCount, MAX(created_at) AS lastVisitAt "
            + "FROM link_visit_log GROUP BY link_id")
    List<LinkVisitStats> selectVisitStats();
}
