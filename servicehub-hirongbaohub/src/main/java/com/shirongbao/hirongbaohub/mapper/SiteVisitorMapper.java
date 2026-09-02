/*
 * auth: hirongbao
 * create: 2026-09-02
 * desc: 访客统计数据访问接口
 */
package com.shirongbao.hirongbaohub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.hirongbaohub.entity.SiteVisitor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SiteVisitorMapper extends BaseMapper<SiteVisitor> {
}
