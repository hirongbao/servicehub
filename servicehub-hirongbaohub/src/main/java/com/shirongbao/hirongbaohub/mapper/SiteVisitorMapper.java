/*
 * auth: hirongbao
 * create: 2026-09-02
 * desc: 访客统计数据访问接口
 */
package com.shirongbao.hirongbaohub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.hirongbaohub.entity.SiteVisitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SiteVisitorMapper extends BaseMapper<SiteVisitor> {
    
    @Select("SELECT COALESCE(SUM(visit_count), 0) FROM site_visitor")
    long sumVisitCount();
}