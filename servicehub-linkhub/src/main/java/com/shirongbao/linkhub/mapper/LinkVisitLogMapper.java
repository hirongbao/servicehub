/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链访问日志数据访问接口
 */
package com.shirongbao.linkhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.linkhub.dto.LinkDailyVisit;
import com.shirongbao.linkhub.entity.LinkVisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LinkVisitLogMapper extends BaseMapper<LinkVisitLog> {

    // 查询指定短链近若干天内的按天访问次数
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS visitDate, COUNT(*) AS visits "
            + "FROM link_visit_log WHERE link_id = #{linkId} AND created_at >= #{since} "
            + "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY visitDate")
    List<LinkDailyVisit> selectDailyVisits(@Param("linkId") Long linkId, @Param("since") java.time.LocalDateTime since);
}
